package de.elegantsoftware.blitzpay.merchant.inbound.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.elegantsoftware.blitzpay.merchant.api.MerchantServicePort
import de.elegantsoftware.blitzpay.merchant.domain.MerchantStatus
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(MerchantController::class)
class MerchantControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var merchantService: MerchantServicePort

    private val objectMapper = jacksonObjectMapper()

    @Test
    @DisplayName("POST /api/merchants should create merchant and return 201")
    fun createMerchant_Returns201() {
        val request = CreateMerchantRequest(
            email = "test@merchant.com",
            businessName = "Test Business",
            defaultCurrency = "USD"
        )

        val publicId = UUID.randomUUID()
        val response = MerchantResponse(
            id = 1L,
            publicId = publicId,
            email = "test@merchant.com",
            businessName = "Test Business",
            status = MerchantStatus.PENDING_VERIFICATION.name,
            settings = MerchantSettingsResponse(
                webhookUrl = null,
                defaultCurrency = "USD"
            ),
            verifiedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(merchantService.createMerchant(request)).thenReturn(response)

        mockMvc.perform(
            post("/api/merchants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.publicId").value(publicId.toString()))
            .andExpect(jsonPath("$.email").value("test@merchant.com"))
            .andExpect(jsonPath("$.businessName").value("Test Business"))
            .andExpect(jsonPath("$.status").value("PENDING_VERIFICATION"))

        verify(merchantService).createMerchant(request)
    }

    @Test
    @DisplayName("POST /api/merchants with invalid email should return 400")
    fun createMerchant_WithInvalidEmail_Returns400() {
        val request = CreateMerchantRequest(
            email = "invalid-email",
            businessName = "Test Business",
            defaultCurrency = "USD"
        )

        mockMvc.perform(
            post("/api/merchants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("GET /api/merchants/{publicId} should return merchant and 200")
    fun getMerchantById_Returns200() {
        val publicId = UUID.randomUUID()
        val response = MerchantResponse(
            id = 1L,
            publicId = publicId,
            email = "test@merchant.com",
            businessName = "Test Business",
            status = MerchantStatus.ACTIVE.name,
            settings = MerchantSettingsResponse(
                webhookUrl = null,
                defaultCurrency = "EUR"
            ),
            verifiedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(merchantService.getMerchant(publicId)).thenReturn(response)

        mockMvc.perform(get("/api/merchants/$publicId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.publicId").value(publicId.toString()))
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        verify(merchantService).getMerchant(publicId)
    }

    @Test
    @DisplayName("POST /api/merchants/{publicId}/verify should verify merchant and return 200")
    fun verifyMerchantById_Returns200() {
        val publicId = UUID.randomUUID()
        val response = MerchantResponse(
            id = 1L,
            publicId = publicId,
            email = "test@merchant.com",
            businessName = "Test Business",
            status = MerchantStatus.ACTIVE.name,
            settings = MerchantSettingsResponse(
                webhookUrl = null,
                defaultCurrency = "EUR"
            ),
            verifiedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        whenever(merchantService.verifyMerchant(publicId)).thenReturn(response)

        mockMvc.perform(post("/api/merchants/$publicId/verify"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ACTIVE"))

        verify(merchantService).verifyMerchant(publicId)
    }

    @Test
    @DisplayName("PUT /api/merchants/{publicId}/settings should update settings and return 204")
    fun merchantSetting_ShouldUpdateSettings_Return204() {
        val publicId = UUID.randomUUID()
        val request = UpdateSettingsRequest(
            defaultCurrency = "USD",
            webhookUrl = "https://webhook.example.com",
            transactionFeePercentage = 2.5
        )
        mockMvc.perform(
            put("/api/merchants/$publicId/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNoContent)

        verify(merchantService).updateMerchantSettings(publicId, request)
    }

    @Test
    @DisplayName("DELETE /api/merchants/{publicId} should deactivate merchant and return 204")
    fun deleteMerchant_ShouldDeactivateMerchant_Returns204() {
        val publicId = UUID.randomUUID()


        mockMvc.perform(delete("/api/merchants/$publicId"))
            .andExpect(status().isNoContent)

        verify(merchantService).deactivateMerchant(publicId)
    }
}
