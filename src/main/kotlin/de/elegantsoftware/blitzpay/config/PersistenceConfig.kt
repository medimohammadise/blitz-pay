package de.elegantsoftware.blitzpay.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan(basePackages = [
    "de.elegantsoftware.blitzpay.common.domain",
    "de.elegantsoftware.blitzpay.merchant.domain",
    "de.elegantsoftware.blitzpay.sales.domain"
])
@EnableJpaRepositories(basePackages = [
    "de.elegantsoftware.blitzpay.merchant.domain",
    "de.elegantsoftware.blitzpay.sales.domain"
])
class PersistenceConfig