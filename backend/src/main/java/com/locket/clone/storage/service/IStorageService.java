package com.locket.clone.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {

    /**
     * Stores the uploaded file and returns the generated object key.
     */
    String store(MultipartFile file);

    /**
     * Loads the bytes of a previously stored object by its key.
     */
    byte[] load(String key);
}
