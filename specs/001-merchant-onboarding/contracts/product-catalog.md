# API Contract: Merchant Product Catalog

**Feature Branch**: `001-merchant-onboarding`
**Date**: 2026-04-20 (updated)
**Module**: `merchant`
**Base path**: `/v1/merchants/{merchantId}/products`

---

## Storage Model

Product images are stored in S3-compatible object storage (MinIO in development, AWS S3 in production).
The backend never accepts raw image URLs from clients — it accepts **storage keys** (object paths) that the client obtained by first calling the upload-url endpoint.

**Image upload flow:**
1. `POST /v1/merchants/{merchantId}/products/{productId}/images/upload-url` → `{ storageKey, uploadUrl, expiresAt }`
2. Client PUTs the binary directly to `uploadUrl` (15-minute TTL, never touches the backend)
3. Client includes `storageKey` in the `imageStorageKeys` list on create or update

**Image download:** `ProductResponse.imageUrls` contains pre-signed GET URLs resolved at request time (~60-minute TTL). Clients should not cache these permanently; re-fetch the product to get fresh URLs.

---

## Security

All endpoints require an authenticated principal. `{merchantId}` in the path is validated against the principal's merchant reference:
- `MERCHANT_APPLICANT` role: may only access their own `{merchantId}`.
- `OPERATIONS_REVIEWER` / `SYSTEM` role: may access any `{merchantId}`.

Requests where `{merchantId}` does not match the principal's entitlement return `403 Forbidden`.

---

## Endpoints

### 1. Create Product

**`POST /v1/merchants/{merchantId}/products`**

Creates a new active product for the given merchant. `imageStorageKeys` is an ordered list — index 0 is the primary display image.

#### Request

```json
{
  "name": "Artisan Coffee Blend 250g",
  "unitPrice": 12.50,
  "imageStorageKeys": [
    "merchant/abc123/products/550e8400/images/a1b2c3d4.jpg",
    "merchant/abc123/products/550e8400/images/e5f6g7h8.jpg"
  ]
}
```

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `name` | string | YES | 1–255 characters, not blank |
| `unitPrice` | number | YES | ≥ 0, up to 4 decimal places |
| `imageStorageKeys` | string[] | NO | Ordered list of S3/MinIO storage keys; empty = no images |

#### Response `201 Created`

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "merchantId": "7b3d9f00-1234-4abc-8765-000000000001",
  "name": "Artisan Coffee Blend 250g",
  "unitPrice": 12.50,
  "imageUrls": [
    "https://storage.blitzpay.io/blitzpay/merchant/abc123/products/550e8400/images/a1b2c3d4.jpg?X-Amz-Signature=...",
    "https://storage.blitzpay.io/blitzpay/merchant/abc123/products/550e8400/images/e5f6g7h8.jpg?X-Amz-Signature=..."
  ],
  "active": true,
  "createdAt": "2026-04-20T10:00:00Z",
  "updatedAt": "2026-04-20T10:00:00Z"
}
```

`imageUrls` are short-lived pre-signed GET URLs (≈60 min). Empty array when no images are set.

#### Error responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | `name` blank, `unitPrice` negative or non-numeric |
| `403 Forbidden` | Principal not entitled to `{merchantId}` |
| `404 Not Found` | `{merchantId}` does not exist |

---

### 2. List Active Products

**`GET /v1/merchants/{merchantId}/products`**

Returns all active products for the merchant. Inactive (soft-deleted) products are excluded.

#### Response `200 OK`

```json
{
  "merchantId": "7b3d9f00-1234-4abc-8765-000000000001",
  "products": [
    {
      "productId": "550e8400-e29b-41d4-a716-446655440000",
      "merchantId": "7b3d9f00-1234-4abc-8765-000000000001",
      "name": "Artisan Coffee Blend 250g",
      "unitPrice": 12.50,
      "imageUrls": [
        "https://storage.blitzpay.io/blitzpay/merchant/abc123/products/550e8400/images/a1b2c3d4.jpg?X-Amz-Signature=..."
      ],
      "active": true,
      "createdAt": "2026-04-20T10:00:00Z",
      "updatedAt": "2026-04-20T10:00:00Z"
    }
  ]
}
```

Empty catalog returns `products: []`, not 404.

---

### 3. Get Product

**`GET /v1/merchants/{merchantId}/products/{productId}`**

Returns a single product. Returns `404` for both non-existent and inactive (soft-deleted) products to prevent enumeration.

#### Response `200 OK`

Same shape as Create response.

#### Error responses

| Status | Condition |
|--------|-----------|
| `403 Forbidden` | Principal not entitled to `{merchantId}` |
| `404 Not Found` | Product not found or soft-deleted |

---

### 4. Update Product

**`PUT /v1/merchants/{merchantId}/products/{productId}`**

Replaces all mutable fields. `imageStorageKeys` replaces the entire image list — pass `[]` to remove all images.

#### Request

```json
{
  "name": "Artisan Coffee Blend 500g",
  "unitPrice": 22.00,
  "imageStorageKeys": []
}
```

#### Response `200 OK`

Same shape as Create response, with updated `updatedAt`.

#### Error responses

| Status | Condition |
|--------|-----------|
| `400 Bad Request` | Validation failure |
| `403 Forbidden` | Principal not entitled |
| `404 Not Found` | Product not found or soft-deleted |

---

### 5. Deactivate Product (Soft Delete)

**`DELETE /v1/merchants/{merchantId}/products/{productId}`**

Sets `active = false`. The product is retained in the database and is no longer returned by list/get operations.

#### Response `204 No Content`

#### Error responses

| Status | Condition |
|--------|-----------|
| `403 Forbidden` | Principal not entitled |
| `404 Not Found` | Product not found or already inactive |

---

### 6. Get Pre-signed Image Upload URL

**`POST /v1/merchants/{merchantId}/products/{productId}/images/upload-url`**

Returns a short-lived S3/MinIO PUT URL for direct browser/mobile upload. The returned `storageKey` must be included in the `imageStorageKeys` list on create or update.

#### Request (optional body)

```json
{
  "contentType": "image/jpeg"
}
```

Omitting the body defaults to `image/jpeg`. Supported values: `image/jpeg`, `image/png`, `image/webp`.

#### Response `200 OK`

```json
{
  "storageKey": "merchant/abc123/products/550e8400/images/a1b2c3d4.jpg",
  "uploadUrl": "https://storage.blitzpay.io/blitzpay/merchant/abc123/products/550e8400/images/a1b2c3d4.jpg?X-Amz-Signature=...",
  "expiresAt": "2026-04-20T10:15:00Z"
}
```

Upload URL expires in 15 minutes. After the PUT completes, include `storageKey` in the product create/update request.

---

## Common Headers

| Header | Required | Notes |
|--------|----------|-------|
| `Content-Type: application/json` | YES for POST/PUT | |
| `Accept: application/json` | Recommended | |

---

## Tenant Isolation Guarantee

Every response for `GET /v1/merchants/{merchantId}/products` contains only products where `merchant_application_id = {merchantId}`:
- Enforced at application layer via Hibernate `tenantFilter`
- Enforced at DB layer via PostgreSQL RLS policy `merchant_tenant_isolation`

A request that receives a `200 OK` listing will never contain products from a different merchant.
