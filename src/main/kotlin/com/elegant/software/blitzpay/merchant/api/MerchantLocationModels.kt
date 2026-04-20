package com.elegant.software.blitzpay.merchant.api

import java.util.UUID

data class SetMerchantLocationRequest(
    val latitude: Double,
    val longitude: Double,
    val geofenceRadiusMeters: Int,
    val googlePlaceId: String? = null
)

data class MerchantLocationResponse(
    val merchantId: UUID,
    val latitude: Double,
    val longitude: Double,
    val geofenceRadiusMeters: Int,
    val googlePlaceId: String?
)

data class NearbyMerchantResponse(
    val merchantId: UUID,
    val legalBusinessName: String,
    val latitude: Double,
    val longitude: Double,
    val geofenceRadiusMeters: Int,
    val googlePlaceId: String?,
    val distanceMeters: Double
)

data class NearbyMerchantsResponse(
    val merchants: List<NearbyMerchantResponse>
)
