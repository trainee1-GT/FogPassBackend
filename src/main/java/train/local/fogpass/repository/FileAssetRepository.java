package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import train.local.fogpass.entity.FileAsset;
import java.util.Optional;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {
    Optional<FileAsset> findByChecksumSha256(String checksumSha256);
}