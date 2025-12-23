@ApplicationModule(
        type = ApplicationModule.Type.OPEN,
        allowedDependencies = {"product", "invoice", "payment", "gateways::api"}
)
package de.elegantsoftware.blitzpay.merchant;

import org.springframework.modulith.ApplicationModule;