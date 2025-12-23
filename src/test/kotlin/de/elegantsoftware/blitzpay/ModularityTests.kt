package de.elegantsoftware.blitzpay

import de.elegantsoftware.blitzpay.BlitzpayApplication
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import java.io.File

class ModularityTests {

    private val modules: ApplicationModules by lazy {
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
        println("Modules count: ${modules.count()}")
        modules.forEach { println("Module: $it") }
        File("modules.txt").writeText(modules.joinToString("\n") { it.name })
        Documenter(modules)
            .writeModulesAsPlantUml() // generates PlantUML diagrams
    }
}

fun main() {
    val modules = ApplicationModules.of(BlitzpayApplication::class.java)
    println("Modules: $modules")
    modules.forEach { println("Module: $it") }
    try {
        modules.verify()
        println("No violations")
    } catch (e: Exception) {
        println("Violations: ${e.message}")
    }
}