package app.service;

import app.model.Achievement;
import app.model.Document;
import app.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class DocumentStorageService {

    private final Path vaultPath;
    private final DocumentRepository documentRepository;

    public DocumentStorageService(@Value("${app.upload.dir:./file-vault}") String uploadDir,
                                  DocumentRepository documentRepository) {
        this.vaultPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.documentRepository = documentRepository;
        try {
            Files.createDirectories(this.vaultPath);
        } catch (IOException e) {
            log.error("Could not create vault storage directory: {}", this.vaultPath, e);
            throw new RuntimeException("Could not initialize vault storage", e);
        }
    }

    public Document storeFile(MultipartFile file, Achievement achievement, String docType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "proof-document";
        }

        // Calculate SHA-256 Checksum for deduplication and integrity
        String checksum = calculateSha256(file.getInputStream());

        // Extract extension safely
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex).toLowerCase();
        }

        // Generate unique stored file name inside the vault
        String storedFileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = this.vaultPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = extension.equalsIgnoreCase(".pdf") ? "application/pdf" : "image/jpeg";
        }

        Document document = Document.builder()
                .achievement(achievement)
                .originalFileName(originalName)
                .storedFileName(storedFileName)
                .fileType(contentType)
                .fileSize(file.getSize())
                .checksumSha256(checksum)
                .docType(docType != null && !docType.isBlank() ? docType : "CERTIFICATE")
                .uploadedAt(LocalDateTime.now())
                .build();

        return documentRepository.save(document);
    }

    public Resource loadFileAsResource(String storedFileName) {
        try {
            Path filePath = this.vaultPath.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found in vault: " + storedFileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Malformed file path: " + storedFileName, ex);
        }
    }

    public String calculateSha256(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
