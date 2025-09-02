package train.local.fogpass.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import train.local.fogpass.config.FileStorageProperties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties properties;

    // Stores file and returns FULL ABSOLUTE PATH on disk
    public String storeFile(MultipartFile file, Long routeId, String storageFileName) throws IOException {
        LocalDate now = LocalDate.now();
        Path base = Paths.get(properties.getLandmarksPath());
        Path targetPath = base.resolve("route_" + routeId)
                .resolve(String.valueOf(now.getYear()))
                .resolve(String.valueOf(now.getMonthValue()));

        Files.createDirectories(targetPath);
        Path filePath = targetPath.resolve(storageFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public void deleteFile(String fullFilePath) throws IOException {
        if (fullFilePath == null || fullFilePath.isBlank()) return;
        Path path = Paths.get(fullFilePath);
        Files.deleteIfExists(path);
    }

    public Resource loadFileAsResource(String fullFilePath) throws IOException {
        Path path = Paths.get(fullFilePath).normalize();
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) throw new FileNotFoundException("File not found: " + fullFilePath);
        return resource;
    }
}