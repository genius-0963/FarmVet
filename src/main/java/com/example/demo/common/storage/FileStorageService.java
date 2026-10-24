package com.example.demo.common.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file, String subdirectory) throws IOException;
    byte[] loadFile(String filePath) throws IOException;
    boolean deleteFile(String filePath);
}
