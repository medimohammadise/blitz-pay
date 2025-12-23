@ApplicationModule(
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {"merchant", "product", "gateways::api","gateways::domain"}
)
package de.elegantsoftware.blitzpay.payment;

import org.springframework.modulith.ApplicationModule;