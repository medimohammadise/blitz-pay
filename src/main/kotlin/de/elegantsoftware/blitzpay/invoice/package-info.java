@ApplicationModule(
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {"merchant", "product", "payment"}
)
package de.elegantsoftware.blitzpay.invoice;

import org.springframework.modulith.ApplicationModule;