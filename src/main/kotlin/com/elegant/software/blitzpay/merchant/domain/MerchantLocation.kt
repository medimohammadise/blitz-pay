package com.elegant.software.blitzpay.merchant.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class MerchantLocation(
    @Column(nullable = false)
    val latitude: Double,

    @Column(nullable = false)
    val longitude: Double,

    @Column(name = "geofence_radius_m", nullable = false)
    val geofenceRadiusMeters: Int,

    @Column(name = "google_place_id")
    val googlePlaceId: String? = null
)
