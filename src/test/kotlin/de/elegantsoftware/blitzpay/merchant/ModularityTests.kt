package de.elegantsoftware.blitzpay.merchant

import de.elegantsoftware.blitzpay.BlitzpayApplication
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class ModularityTests {

    private val modules by lazy {
        ApplicationModules.of(BlitzpayApplication::class.java)
    }

    @Test
    fun verifyModularity() {
        modules.verify()
    }

    @Test
    fun writeDocumentation() {
        Documenter(modules)
            .writeModulesAsPlantUml() // generates PlantUML diagrams
    }
}
