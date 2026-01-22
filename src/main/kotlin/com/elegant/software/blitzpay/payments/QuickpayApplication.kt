package com.elegant.software.quickpay.payments

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QuickpayApplication

fun main(args: Array<String>) {
	runApplication<QuickpayApplication>(*args)
}
