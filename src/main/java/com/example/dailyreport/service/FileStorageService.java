package com.example.dailyreport.service;

import com.example.dailyreport.config.FileStorageProperties;
import com.example.dailyreport.domain.RequestFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    public FileStorageService(FileStorageProperties properties) throws IOException {
        this.fileStorageLocation = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    public RequestFile store(MultipartFile file) throws IOException {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "_" + originalName;
        Path targetLocation = this.fileStorageLocation.resolve(storedName);
        Files.copy(file.getInputStream(), targetLocation);

        RequestFile requestFile = new RequestFile();
        requestFile.setOriginalName(originalName);
        requestFile.setStoredName(storedName);
        requestFile.setSize(file.getSize());
        requestFile.setMimeType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        return requestFile;
    }

    public Resource loadAsResource(String storedName) throws MalformedURLException {
        Path filePath = this.fileStorageLocation.resolve(storedName).normalize();
        return new UrlResource(filePath.toUri());
    }
}
