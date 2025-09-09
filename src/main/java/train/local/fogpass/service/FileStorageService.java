package train.local.fogpass.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import train.local.fogpass.config.FileStorageProperties;
import train.local.fogpass.entity.Route;
import train.local.fogpass.entity.Section;
import train.local.fogpass.entity.Division;
import train.local.fogpass.entity.Zone;
import train.local.fogpass.repository.RouteRepository;

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
    private final RouteRepository routeRepository;

    // Stores file into the uploads bucket with year/month partitioning and returns absolute path
    public String storeFile(MultipartFile file, Long routeId, String storageFileName) throws IOException {
        LocalDate now = LocalDate.now();
        Path base = Paths.get(properties.getUploads()).resolve("single")
                .resolve(String.valueOf(now.getYear()))
                .resolve(String.format("%02d", now.getMonthValue()));

        Files.createDirectories(base);
        Path filePath = base.resolve(storageFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath.toString();
    }

    // Save directly to zones/{zone}/{division}/{section}/{route}/{finalFileName}
    public String storeFileToZonesForRoute(MultipartFile file, Long routeId, String finalFileName) throws IOException {
        Route route = routeRepository.findById(routeId).orElseThrow();
        String relative = buildRouteRelativePath(route);
        Path destDir = Paths.get(properties.getRoot()).resolve(relative);
        Files.createDirectories(destDir);
        Path dest = destDir.resolve(finalFileName);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString();
    }

    // Move processed file to canonical zones/{zone}/{division}/{section}/{route}/{finalFileName}
    public String moveToProcessedForRoute(String currentPath, Long routeId, String finalFileName) throws IOException {
        Route route = routeRepository.findById(routeId).orElseThrow();
        String relative = buildRouteRelativePath(route);
        Path destDir = Paths.get(properties.getProcessed()).resolve(relative);
        Files.createDirectories(destDir);
        Path src = Paths.get(currentPath);
        Path dest = destDir.resolve(finalFileName);
        Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString();
    }

    private String buildRouteRelativePath(Route route) {
        Section section = route.getSection();
        Division division = section != null ? section.getDivision() : null;
        Zone zone = division != null ? division.getZone() : null;
        String zoneName = zone != null && zone.getZonename() != null ? zone.getZonename() : "UNKNOWN_ZONE";
        String divisionName = division != null && division.getName() != null ? division.getName() : "UNKNOWN_DIVISION";
        String sectionName = section != null && section.getName() != null ? section.getName() : "UNKNOWN_SECTION";
        String routeCode = route.getRouteCode() != null ? route.getRouteCode() : "UNKNOWN_ROUTE";
        return Paths.get("zones",
                sanitize(zoneName),
                sanitize(divisionName),
                sanitize(sectionName),
                sanitize(routeCode))
                .toString();
    }

    private String sanitize(String part) {
        // Basic filesystem-friendly sanitizer
        return part.trim().replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
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