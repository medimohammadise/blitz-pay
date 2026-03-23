package com.elegant.software.blitzpay.contract

import com.elegant.software.blitzpay.payments.QuickpayApplication
import io.restassured.module.webtestclient.RestAssuredWebTestClient
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    classes = [QuickpayApplication::class],
    properties = [
        "truelayer.clientId=test-client-id",
        "truelayer.clientSecret=test-client-secret",
        "truelayer.keyId=test-key-id",
        "truelayer.privateKeyPath=truelayer-test-private-key.pem",
        "truelayer.merchantAccountId=test-merchant-account-id",
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
            "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
            "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration," +
            "org.springframework.modulith.events.jpa.JpaEventPublicationAutoConfiguration," +
            "org.springframework.modulith.events.config.EventPublicationAutoConfiguration"
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
abstract class ContractVerifierBase {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setupRestAssured() {
        RestAssuredWebTestClient.webTestClient(webTestClient)
    }
}
