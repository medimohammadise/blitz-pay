package de.elegantsoftware.blitzpay.truelayer.api

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.repository.query.Param

data class TrueLayerPaymentRequest(
    @Param("expires_at")
    val expiresAt: String,

    val reference: String,

    @Param("return_uri")
    val returnUri: String,

    @Param("payment_configuration")
    val paymentConfiguration: PaymentConfiguration,

    @Param("product_items")
    val productItems: List<ProductItem>,

    val type: String = "single_payment"
) {
    data class PaymentConfiguration(
        @Param("amount_in_minor")
        val amountInMinor: Long,

        val currency: String,

        @Param("payment_method")
        val paymentMethod: PaymentMethod,

        val user: PaymentUser
    )

    data class PaymentMethod(
        val type: String = "bank_transfer",

        @Param("provider_selection")
        val providerSelection: ProviderSelection = ProviderSelection(),

        val beneficiary: Beneficiary
    )

    data class ProviderSelection(
        val type: String = "user_selected"
    )

    data class Beneficiary(
        val type: String = "merchant_account",

        @Param("merchant_account_id")
        val merchantAccountId: String
    )

    data class PaymentUser(
        val id: String,
        val name: String,
        val email: String? = null,
        val phone: String? = null,

        @Param("date_of_birth")
        val dateOfBirth: String? = null,

        val address: Address? = null
    )

    data class Address(
        @Param("address_line1")
        val addressLine1: String,

        val city: String,
        val state: String,
        val zip: String,

        @Param("country_code")
        val countryCode: String
    )

    data class ProductItem(
        val name: String,

        @Param("price_in_minor")
        val priceInMinor: Long,

        val quantity: Int,

        val url: String? = null
    )
}

data class TrueLayerPaymentResponse(
    val id: String,
    val uri: String
)