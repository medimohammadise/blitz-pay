package de.elegantsoftware.blitzpay

import de.elegantsoftware.blitzpay.app.BlitzpayApplication
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class ModularityTests {

    private val modules by lazy {
        ApplicationModules.of(BlitzpayApplication::class.java)
    }

    @Test
    fun verifyModularity() {
        try {
            modules.verify()
        } catch (e: Exception) {
            println("Modularity violations: ${e.message}")
            throw e
        }
    }

    @Test
    fun writeDocumentation() {
        Documenter(modules)
            .writeModulesAsPlantUml() // generates PlantUML diagrams
    }
}

fun main() {
    val modules = ApplicationModules.of(BlitzpayApplication::class.java)
    println("Modules: $modules")
    try {
        modules.verify()
        println("No violations")
    } catch (e: Exception) {
        println("Violations: ${e.message}")
    }
}