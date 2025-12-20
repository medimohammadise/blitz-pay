package de.elegantsoftware.blitzpay.merchant.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    open var id: Long = 0

    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    open var publicId: UUID = UUID.randomUUID()

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now()

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseEntity
        return id == other.id && publicId == other.publicId
    }

    override fun hashCode(): Int {
        return 31 * id.hashCode() + publicId.hashCode()
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}(id=$id, publicId=$publicId)"
    }
}

// Optional: Separate interface if you need more flexibility
interface BaseEntityInterface {
    val id: Long
    val publicId: UUID
    val createdAt: LocalDateTime
    var updatedAt: LocalDateTime
}