package ca.mcgill.ecse321.gamenight.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import jakarta.annotation.PostConstruct;

@Service
public class FileStorageService {
    private final Path root = Paths.get("uploads").toAbsolutePath().normalize();
    private final List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif");
    private final long maxFileSize = 2 * 1024 * 1024; // 2MB

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root.resolve("games"));
    }

    public String store(MultipartFile file, String prefix) throws IOException {
        // Validation
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new IOException("File size exceeds maximum limit of 2MB");
        }

        // Get file extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!allowedExtensions.contains(extension)) {
            throw new IOException("Invalid file type. Allowed types: " + allowedExtensions);
        }

        // Generate unique filename
        String filename = String.format("%s-%s.%s", prefix, UUID.randomUUID(), extension);
        Path target = root.resolve("games/" + filename);

        // Store file
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return "games/" + filename;
    }

    public Resource load(String path) throws IOException {
        Path file = root.resolve(path);
        Resource resource = new UrlResource(file.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Could not read file: " + path);
        }
    }

    public void delete(String path) throws IOException {
        Path file = root.resolve(path);
        Files.deleteIfExists(file);
    }

    public String getContentType(String path) throws IOException {
        return Files.probeContentType(root.resolve(path));
    }
}