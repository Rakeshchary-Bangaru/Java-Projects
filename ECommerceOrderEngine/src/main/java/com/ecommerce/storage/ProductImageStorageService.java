
package com.ecommerce.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductImageStorageService {

    StoredProductImage store(MultipartFile file)
            throws IOException;

    Resource load(String storageKey)
            throws IOException;

    void delete(String storageKey)
            throws IOException;

    record StoredProductImage(
            String storageKey,
            String contentType,
            long fileSize
    ) {
    }
}
