package com.snim.demandesrh.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PhotoService {

    private final String uploadDir = "uploads/";

    public String uploadPhoto(MultipartFile photoFile) throws IOException {
        if (photoFile.isEmpty()) {
            throw new IOException("Failed to store empty file.");
        }

        String fileName = UUID.randomUUID().toString() + "_" + photoFile.getOriginalFilename();
        Path destinationPath = Paths.get(uploadDir).resolve(fileName).normalize().toAbsolutePath();

        Files.copy(photoFile.getInputStream(), destinationPath);

        return destinationPath.toString();
    }
}

