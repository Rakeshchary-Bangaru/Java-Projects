
package com.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.product-images")
public record ProductImageStorageProperties(
        String storageDirectory
) {

    /*
     * Default local storage directory.
     *
     * This is used when Spring Boot starts without
     * an explicitly configured storage directory,
     * including standalone startup tests.
     */
    public ProductImageStorageProperties {

        if (storageDirectory == null ||
                storageDirectory.isBlank()) {

            storageDirectory =
                    "./local-uploads/product-images";
        }
    }
}
