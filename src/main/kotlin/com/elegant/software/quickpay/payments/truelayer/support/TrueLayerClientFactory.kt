package com.elegant.software.quickpay.payments.truelayer.support

import com.truelayer.java.ClientCredentials
import com.truelayer.java.Environment
import com.truelayer.java.SigningOptions
import com.truelayer.java.TrueLayerClient
import java.nio.file.Files
import java.nio.file.Path



class TrueLayerClientFactory(
    private val clientId: String,
    private val clientSecret: String,
    private val keyId: String,
    private val privateKeyPath: String
) {
    fun create(): TrueLayerClient {
        val privateKeyBytes = run {
            val path = Path.of(privateKeyPath)
            if (Files.exists(path)) {
                Files.readAllBytes(path)
            } else {
                val resourceStream = Thread.currentThread().contextClassLoader
                    .getResourceAsStream(privateKeyPath)
                    ?: this::class.java.classLoader.getResourceAsStream(privateKeyPath)
                    ?: throw IllegalStateException("Private key not found at path '$privateKeyPath' (filesystem) or on classpath")
                resourceStream.use { it.readAllBytes() }
            }
        }
        return TrueLayerClient.New()
            .environment(Environment.sandbox())
            .withHttpLogs()
            .clientCredentials(
                ClientCredentials.builder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build()
            )
            .signingOptions(
                SigningOptions.builder()
                    .keyId(keyId)
                    .privateKey(privateKeyBytes)
                    .build()
            )
            .build()
    }
}