package com.example.demo.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path baseStoragePath;

    public LocalFileStorageService(@Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.baseStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create the base storage directory for file uploads.", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subdirectory) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store an empty file.");
        }

        String rawFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (rawFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid path sequence in filename: " + rawFilename);
        }

        String uniqueFilename = UUID.randomUUID() + "_" + rawFilename;
        Path targetDir = this.baseStoragePath.resolve(subdirectory).normalize();
        Files.createDirectories(targetDir);

        Path targetLocation = targetDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return subdirectory + File.separator + uniqueFilename;
    }

    @Override
    public byte[] loadFile(String relativePath) throws IOException {
        Path targetFile = this.baseStoragePath.resolve(relativePath).normalize();
        if (!targetFile.startsWith(this.baseStoragePath)) {
            throw new SecurityException("Access to path outside base storage directory is forbidden.");
        }
        return Files.readAllBytes(targetFile);
    }

    @Override
    public boolean deleteFile(String relativePath) {
        try {
            Path targetFile = this.baseStoragePath.resolve(relativePath).normalize();
            if (!targetFile.startsWith(this.baseStoragePath)) {
                return false;
            }
            return Files.deleteIfExists(targetFile);
        } catch (IOException e) {
            return false;
        }
    }
}
