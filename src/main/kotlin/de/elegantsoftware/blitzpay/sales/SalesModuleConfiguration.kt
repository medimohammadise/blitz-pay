package de.elegantsoftware.blitzpay.sales

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ComponentScan
@EnableJpaRepositories(basePackages = ["de.elegantsoftware.blitzpay.sales.infrastructure.persistence"])
class SalesModuleConfiguration