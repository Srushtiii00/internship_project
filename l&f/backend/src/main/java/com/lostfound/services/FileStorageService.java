package com.lostfound.services;

import com.lostfound.exceptions.BadRequestException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/pjpeg", "image/png", "image/webp", "image/gif"
    );

    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BadRequestException("Invalid file name");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only image files (.jpg, .jpeg, .png, .webp, .gif) are allowed");
        }

        validateFileContent(file);

        String filename = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(uploadDir).resolve(filename);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    private void validateFileContent(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid file type: Content-Type '" + contentType + "' is not supported");
        }

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header, 0, 12);
            if (read < 4) {
                throw new BadRequestException("Corrupted or empty image file");
            }

            boolean isValidImage = false;
            // JPEG: FF D8 FF
            if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
                isValidImage = true;
            }
            // PNG: 89 50 4E 47
            else if ((header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                isValidImage = true;
            }
            // GIF: 47 49 46 38 ("GIF8")
            else if (header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8') {
                isValidImage = true;
            }
            // WEBP: RIFF....WEBP
            else if (read >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                    && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
                isValidImage = true;
            }

            if (!isValidImage) {
                throw new BadRequestException("File signature verification failed: File content is not a valid image");
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to read image stream for validation");
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/")) {
            return;
        }

        String filename = fileUrl.substring("/uploads/".length());
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
}
