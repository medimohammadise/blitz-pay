package de.elegantsoftware.blitzpay.truelayer.inbound


import de.elegantsoftware.blitzpay.truelayer.outbound.TrueLayerTokenService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TrueLayerTokenController(
    private val tokenService: TrueLayerTokenService
) {

    @GetMapping("/truelayer/token")
    fun getToken(): Map<String, String> {
        val token = tokenService.fetchToken()
        return mapOf("access_token" to token)
    }
}
