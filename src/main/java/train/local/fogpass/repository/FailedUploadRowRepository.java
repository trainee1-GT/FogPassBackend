package train.local.fogpass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.FailedUploadRow;

import java.util.UUID;

@Repository
public interface FailedUploadRowRepository extends JpaRepository<FailedUploadRow, Long> {
    Page<FailedUploadRow> findByJobId(UUID jobId, Pageable pageable);
}