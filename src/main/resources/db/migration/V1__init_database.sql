-- Drop existing tables in correct order (if needed)
DROP TABLE IF EXISTS invoice_items CASCADE;
DROP TABLE IF EXISTS product_categories CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS merchants CASCADE;

-- FIRST: Create merchants table (must exist before products)
CREATE TABLE IF NOT EXISTS merchants (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    default_currency VARCHAR(3) DEFAULT 'EUR',
    transaction_fee_percentage DECIMAL(5,2) DEFAULT 2.5,
    api_key VARCHAR(255),
    webhook_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_merchants_status CHECK (status IN (
        'PENDING_VERIFICATION', 'ACTIVE', 'INACTIVE', 'SUSPENDED'
    )),
    CONSTRAINT chk_merchants_fee CHECK (
        transaction_fee_percentage >= 0 AND transaction_fee_percentage <= 100
    )
);

-- SECOND: Products table (references merchants.id - internal ID)
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    merchant_id BIGINT NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price_amount DECIMAL(19,2) NOT NULL,
    price_currency VARCHAR(3) DEFAULT 'EUR',
    stock_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    tax_rate DECIMAL(5,3),
    tax_code VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_products_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK')),
    CONSTRAINT chk_products_price CHECK (price_amount >= 0),
    CONSTRAINT chk_products_stock CHECK (stock_quantity >= 0),
    CONSTRAINT chk_products_tax_rate CHECK (tax_rate >= 0 AND tax_rate <= 1),
    CONSTRAINT uniq_products_merchant_sku UNIQUE (merchant_id, sku)
);

-- THIRD: Product categories table (for many-to-many relationship)
CREATE TABLE IF NOT EXISTS product_categories (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (product_id, category)
);

-- FOURTH: Invoices table (stores merchant's public ID, not internal ID)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(100) UNIQUE NOT NULL,
    -- Merchant info (stores public ID)
    merchant_id UUID NOT NULL,
    merchant_name VARCHAR(255) NOT NULL,
    merchant_email VARCHAR(255) NOT NULL,
    -- Invoice details
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    issue_date DATE,
    due_date DATE NOT NULL,
    -- Billing address
    billing_street VARCHAR(255),
    billing_city VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country CHAR(2),
    -- Notes
    notes TEXT,
    -- Totals
    subtotal_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    subtotal_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    tax_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    tax_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    paid_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    paid_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    payment_date DATE,
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_invoices_status CHECK (status IN ('DRAFT', 'ISSUED', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT chk_invoices_amounts CHECK (
        total_amount = subtotal_amount + tax_amount AND
        total_amount >= 0 AND
        subtotal_amount >= 0 AND
        tax_amount >= 0 AND
        paid_amount >= 0 AND
        paid_amount <= total_amount
    )
);

-- FIFTH: Invoice items table
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    -- Product info (snapshot at invoice time)
    product_id UUID NOT NULL,
    product_sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_tax_rate DECIMAL(5,3),
    -- Item details
    quantity INT NOT NULL,
    unit_price_amount DECIMAL(19,2) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_invoice_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_invoice_items_unit_price CHECK (unit_price_amount >= 0)
);

-- ============================================
-- CREATE INDEXES
-- ============================================

-- Merchants indexes
CREATE INDEX IF NOT EXISTS idx_merchants_public_id ON merchants(public_id);
CREATE INDEX IF NOT EXISTS idx_merchants_email ON merchants(email);
CREATE INDEX IF NOT EXISTS idx_merchants_status ON merchants(status);

-- Products indexes
CREATE INDEX IF NOT EXISTS idx_products_public_id ON products(public_id);
CREATE INDEX IF NOT EXISTS idx_products_merchant_id ON products(merchant_id);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_sku_merchant ON products(merchant_id, sku);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price_amount);
CREATE INDEX IF NOT EXISTS idx_products_stock ON products(stock_quantity);

-- Invoices indexes
CREATE INDEX IF NOT EXISTS idx_invoices_public_id ON invoices(public_id);
CREATE INDEX IF NOT EXISTS idx_invoices_invoice_number ON invoices(invoice_number);
CREATE INDEX IF NOT EXISTS idx_invoices_merchant_id ON invoices(merchant_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_due_date ON invoices(due_date);
CREATE INDEX IF NOT EXISTS idx_invoices_issue_date ON invoices(issue_date);
CREATE INDEX IF NOT EXISTS idx_invoices_created_at ON invoices(created_at);

-- Invoice items indexes
CREATE INDEX IF NOT EXISTS idx_invoice_items_public_id ON invoice_items(public_id);
CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice_id ON invoice_items(invoice_id);
CREATE INDEX IF NOT EXISTS idx_invoice_items_product_id ON invoice_items(product_id);

-- Product categories indexes
CREATE INDEX IF NOT EXISTS idx_product_categories_product_id ON product_categories(product_id);
CREATE INDEX IF NOT EXISTS idx_product_categories_category ON product_categories(category);

-- ============================================
-- CREATE TRIGGERS
-- ============================================

-- Function to update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Merchants trigger
CREATE TRIGGER update_merchants_updated_at
    BEFORE UPDATE ON merchants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Products trigger
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Invoices trigger
CREATE TRIGGER update_invoices_updated_at
    BEFORE UPDATE ON invoices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Invoice items trigger
CREATE TRIGGER update_invoice_items_updated_at
    BEFORE UPDATE ON invoice_items
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- OPTIONAL: CREATE VIEWS
-- ============================================

-- View for product availability
CREATE OR REPLACE VIEW available_products AS
SELECT
    p.id,
    p.public_id,
    p.merchant_id,
    p.sku,
    p.name,
    p.description,
    p.price_amount,
    p.price_currency,
    p.stock_quantity,
    p.status,
    p.tax_rate,
    p.tax_code,
    p.created_at,
    p.updated_at
FROM products p
WHERE p.status = 'ACTIVE' AND p.stock_quantity > 0;

-- View for overdue invoices
CREATE OR REPLACE VIEW overdue_invoices AS
SELECT
    i.id,
    i.public_id,
    i.invoice_number,
    i.merchant_id,
    i.merchant_name,
    i.merchant_email,
    i.status,
    i.issue_date,
    i.due_date,
    i.total_amount,
    i.total_currency,
    i.paid_amount,
    i.paid_currency,
    i.created_at,
    i.updated_at
FROM invoices i
WHERE i.status = 'ISSUED'
  AND i.due_date < CURRENT_DATE
  AND i.paid_amount < i.total_amount;