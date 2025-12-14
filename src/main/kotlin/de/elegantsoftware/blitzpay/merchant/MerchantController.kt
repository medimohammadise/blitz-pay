package de.elegantsoftware.blitzpay.merchant

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/merchants")
class MerchantController(private val merchantService: MerchantService) {

    @PostMapping
    fun create(@RequestBody merchant: Merchant): ResponseEntity<Merchant> =
        ResponseEntity.ok(merchantService.create(merchant))

    @GetMapping
    fun getAll(): ResponseEntity<List<Merchant>> =
        ResponseEntity.ok(merchantService.findAll())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Merchant> =
        ResponseEntity.ok(merchantService.findById(id))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        merchantService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
