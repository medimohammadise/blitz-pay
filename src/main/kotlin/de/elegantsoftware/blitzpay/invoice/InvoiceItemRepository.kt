package de.elegantsoftware.blitzpay.invoice

import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceItemRepository : JpaRepository<InvoiceItem, Long>
