package com.elegant.software.blitzpay.support

import com.elegant.software.blitzpay.payments.push.config.ExpoPushProperties
import com.elegant.software.blitzpay.payments.push.internal.ExpoMessage
import com.elegant.software.blitzpay.payments.push.internal.ExpoPushClient
import com.elegant.software.blitzpay.payments.push.internal.ExpoTicket
import com.elegant.software.blitzpay.storage.PresignedUpload
import com.elegant.software.blitzpay.storage.StorageService
import jakarta.persistence.EntityManagerFactory
import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant

@TestConfiguration
@Profile("contract-test")
class ContractTestConfig {

    @Bean
    @Primary
    fun mockExpoPushClient(properties: ExpoPushProperties, expoWebClient: WebClient): ExpoPushClient =
        object : ExpoPushClient(properties, expoWebClient) {
            override fun send(messages: List<ExpoMessage>): List<ExpoTicket> =
                messages.map { ExpoTicket(it.to, "contract-ticket-${it.to.hashCode()}", ExpoTicket.Status.OK, null) }
        }

    @Bean(name = ["entityManagerFactory"])
    @Primary
    fun mockEntityManagerFactory(): EntityManagerFactory = mock()

    @Bean
    @Primary
    fun mockStorageService(): StorageService = object : StorageService {
        override fun presignUpload(storageKey: String, contentType: String, ttlMinutes: Long) =
            PresignedUpload(storageKey, "http://localhost:9000/blitzpay/$storageKey", Instant.now().plusSeconds(ttlMinutes * 60))
        override fun presignDownload(storageKey: String, ttlMinutes: Long) =
            "http://localhost:9000/blitzpay/$storageKey"
        override fun delete(storageKey: String) = Unit
    }
}
