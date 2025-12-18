package de.elegantsoftware.blitzpay.merchant.inbound.web

import de.elegantsoftware.blitzpay.merchant.api.MerchantServicePort
import de.elegantsoftware.blitzpay.merchant.inbound.web.dto.*
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/merchants")
@Tag(
    name = "Merchant",
    description = "Merchant management APIs"
)
class MerchantController(
    private val merchantService: MerchantServicePort
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMerchant(@Valid @RequestBody request: CreateMerchantRequest): MerchantResponse {
        return merchantService.createMerchant(request)
    }

    @GetMapping("/{id}")
    fun getMerchant(@PathVariable id: UUID): MerchantResponse {
        return merchantService.getMerchant(id)
    }

    @PostMapping("/{id}/verify")
    fun verifyMerchant(@PathVariable id: UUID): MerchantResponse {
        return merchantService.verifyMerchant(id)
    }

    @PatchMapping("/{id}/settings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateSettings(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateSettingsRequest
    ) {
        merchantService.updateMerchantSettings(id, request)
    }
}