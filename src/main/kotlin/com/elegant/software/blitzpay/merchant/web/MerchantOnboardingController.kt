package com.elegant.software.blitzpay.merchant.web

import com.elegant.software.blitzpay.merchant.api.MerchantGateway
import com.elegant.software.blitzpay.merchant.api.MerchantSummary
import com.elegant.software.blitzpay.merchant.domain.BusinessProfile
import com.elegant.software.blitzpay.merchant.domain.MerchantApplication
import com.elegant.software.blitzpay.merchant.domain.MerchantOnboardingStatus
import com.elegant.software.blitzpay.merchant.domain.PrimaryContact
import com.elegant.software.blitzpay.merchant.repository.MerchantApplicationRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class CreateMerchantRequest(
    val applicationReference: String,
    val legalBusinessName: String,
    val businessType: String,
    val registrationNumber: String,
    val operatingCountry: String,
    val primaryBusinessAddress: String,
    val contactFullName: String,
    val contactEmail: String,
    val contactPhoneNumber: String
)

@Tag(name = "Merchant Onboarding", description = "Endpoints for merchant onboarding and lifecycle")
@RestController
@RequestMapping("/{version:v\\d+(?:\\.\\d+)*}/merchants", version = "1")
class MerchantOnboardingController(
    private val repository: MerchantApplicationRepository,
    private val gateway: MerchantGateway
) {

    @Operation(summary = "Create a new merchant application in DRAFT status")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateMerchantRequest): MerchantSummary {
        val application = MerchantApplication(
            applicationReference = request.applicationReference,
            businessProfile = BusinessProfile(
                legalBusinessName = request.legalBusinessName,
                businessType = request.businessType,
                registrationNumber = request.registrationNumber,
                operatingCountry = request.operatingCountry,
                primaryBusinessAddress = request.primaryBusinessAddress
            ),
            primaryContact = PrimaryContact(
                fullName = request.contactFullName,
                email = request.contactEmail,
                phoneNumber = request.contactPhoneNumber
            )
        )
        val saved = repository.save(application)
        return MerchantSummary(
            applicationId = saved.id,
            applicationReference = saved.applicationReference,
            registrationNumber = saved.businessProfile.registrationNumber,
            status = saved.status,
            submittedAt = saved.submittedAt,
            lastUpdatedAt = saved.lastUpdatedAt
        )
    }

    @Operation(summary = "Directly activate a merchant application (skips onboarding flow)")
    @PostMapping("/{id}/activate")
    fun activate(@PathVariable id: UUID): MerchantSummary {
        val application = repository.findById(id)
            .orElseThrow { NoSuchElementException("Merchant application not found: $id") }
        
        application.registerDirect()
        repository.save(application)
        
        return MerchantSummary(
            applicationId = application.id,
            applicationReference = application.applicationReference,
            registrationNumber = application.businessProfile.registrationNumber,
            status = application.status,
            submittedAt = application.submittedAt,
            lastUpdatedAt = application.lastUpdatedAt
        )
    }

    @Operation(summary = "Get merchant application details")
    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): MerchantSummary {
        val application = repository.findById(id)
            .orElseThrow { NoSuchElementException("Merchant application not found: $id") }
            
        return MerchantSummary(
            applicationId = application.id,
            applicationReference = application.applicationReference,
            registrationNumber = application.businessProfile.registrationNumber,
            status = application.status,
            submittedAt = application.submittedAt,
            lastUpdatedAt = application.lastUpdatedAt
        )
    }
}
