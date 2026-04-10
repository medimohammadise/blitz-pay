package com.elegant.software.blitzpay.support

import com.elegant.software.blitzpay.invoice.internal.InvoiceRecipient
import com.elegant.software.blitzpay.invoice.internal.PaymentStatus
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.math.BigDecimal
import java.time.Instant

object InvoiceGenerationActivityFixtureLoader {

    private val objectMapper: JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .addModule(JavaTimeModule())
        .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private val fixture: InvoiceGenerationActivityFixtureScenario by lazy {
        val resource = requireNotNull(this::class.java.classLoader.getResourceAsStream(FIXTURE_PATH)) {
            "Missing fixture file: $FIXTURE_PATH"
        }
        resource.use { objectMapper.readValue(it) }
    }

    fun command(): InvoiceGenerationActivityFixtureCommand = fixture.command

    fun expectations(): InvoiceGenerationActivityFixtureExpectations = fixture.expectations

    private const val FIXTURE_PATH = "testdata/invoice-generation-activity/canonical-activity.json"
}

data class InvoiceGenerationActivityFixtureScenario(
    val scenarioId: String,
    val description: String,
    val domain: String,
    val tags: List<String>,
    val command: InvoiceGenerationActivityFixtureCommand,
    val expectations: InvoiceGenerationActivityFixtureExpectations,
)

data class InvoiceGenerationActivityFixtureCommand(
    val invoiceNumber: String,
    val createdAt: Instant,
    val amount: BigDecimal,
    val currency: String,
    val recipients: List<InvoiceRecipient>,
    val paymentStatus: PaymentStatus,
)

data class InvoiceGenerationActivityFixtureExpectations(
    val amountMinorUnits: Long,
    val recipientCount: Int,
    val firstRecipientEmail: String,
    val secondRecipientEmail: String,
)
