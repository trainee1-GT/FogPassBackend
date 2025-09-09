package train.local.fogpass.service.retention;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import train.local.fogpass.config.FileStorageProperties;
import train.local.fogpass.config.RetentionProperties;
import train.local.fogpass.entity.FileAsset;
import train.local.fogpass.repository.FileAssetRepository;

import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DataRetentionService {

    private final FileAssetRepository fileAssetRepository;
    private final FileStorageProperties storageProperties;
    private final RetentionProperties retentionProperties;

    // Run nightly at 02:30
    @Scheduled(cron = "0 30 2 * * *")
    @Transactional
    public void applyRetention() {
        List<FileAsset> all = fileAssetRepository.findAll(); // For large datasets, replace with streaming/paging
        Instant now = Instant.now();

        for (FileAsset a : all) {
            RetentionProperties.Rule rule = retentionProperties.ruleFor(a.getAssetType().name());
            Instant created = a.getCreatedAt() != null ? a.getCreatedAt() : now;
            Instant archiveAt = created.plus(rule.getArchiveAfterDays(), ChronoUnit.DAYS);
            Instant purgeAt = created.plus(rule.getPurgeAfterDays(), ChronoUnit.DAYS);

            try {
                if (a.getStatus() == FileAsset.AssetStatus.PROCESSED && now.isAfter(archiveAt)) {
                    // archive file if still at uploads/processed
                    moveIfExists(a.getStoragePath(), storageProperties.getArchive());
                    a.setStatus(FileAsset.AssetStatus.ARCHIVED);
                    fileAssetRepository.save(a);
                }
                if (now.isAfter(purgeAt)) {
                    // delete physical file and record
                    deleteIfExists(a.getStoragePath());
                    fileAssetRepository.delete(a);
                }
            } catch (Exception ex) {
                log.warn("Retention handling failed for asset {}: {}", a.getId(), ex.getMessage());
            }
        }
    }

    private void moveIfExists(String fullPath, String archiveRoot) {
        if (fullPath == null || fullPath.isBlank()) return;
        Path src = Paths.get(fullPath);
        if (!Files.exists(src)) return;

        Path archiveBase = Paths.get(archiveRoot);
        try {
            Files.createDirectories(archiveBase);
            Path dest = archiveBase.resolve(src.getFileName().toString());
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Archive move failed: " + e.getMessage(), e);
        }
    }

    private void deleteIfExists(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) return;
        try {
            Files.deleteIfExists(Paths.get(fullPath));
        } catch (Exception e) {
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }
}