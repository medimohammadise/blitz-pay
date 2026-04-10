package com.elegant.software.blitzpay.invoice.internal

import com.elegant.software.blitzpay.support.InvoiceGenerationActivityFixtureLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InvoiceGenerationActivityRepositoryTest(
    @Autowired private val invoiceGenerationActivityRepository: InvoiceGenerationActivityRepository
) {

    @Test
    fun `persists invoice generation activity with recipients and status`() {
        val command = InvoiceGenerationActivityFixtureLoader.command()
        val expectations = InvoiceGenerationActivityFixtureLoader.expectations()

        val saved = invoiceGenerationActivityRepository.save(
            InvoiceGenerationActivity(
                invoiceNumber = command.invoiceNumber,
                createdAt = command.createdAt,
                amountMinorUnits = expectations.amountMinorUnits,
                currency = command.currency,
                recipients = command.recipients.toCollection(linkedSetOf()),
                paymentStatus = command.paymentStatus
            )
        )

        val reloaded = invoiceGenerationActivityRepository.findById(saved.id).orElseThrow()

        assertEquals(command.invoiceNumber, reloaded.invoiceNumber)
        assertEquals(command.createdAt, reloaded.createdAt)
        assertEquals(expectations.amountMinorUnits, reloaded.amountMinorUnits)
        assertEquals(command.amount, reloaded.amount())
        assertEquals(command.currency, reloaded.currency)
        assertEquals(command.paymentStatus, reloaded.paymentStatus)
        assertEquals(expectations.recipientCount, reloaded.recipients.size)
        assertEquals(expectations.firstRecipientEmail, reloaded.recipients.first().email)
        assertEquals(expectations.secondRecipientEmail, reloaded.recipients.last().email)
    }

    @Test
    fun `finds activities by payment status after transition`() {
        val command = InvoiceGenerationActivityFixtureLoader.command()

        val activity = invoiceGenerationActivityRepository.save(
            InvoiceGenerationActivity(
                invoiceNumber = "${command.invoiceNumber}-received",
                createdAt = command.createdAt,
                amountMinorUnits = 12500,
                currency = command.currency,
                recipients = command.recipients.toCollection(linkedSetOf())
            )
        )

        activity.markReceived()
        invoiceGenerationActivityRepository.flush()

        val received = invoiceGenerationActivityRepository.findAllByPaymentStatusOrderByCreatedAtDesc(PaymentStatus.RECEIVED)

        assertTrue(received.any { it.id == activity.id })
    }

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun registerDataSourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
