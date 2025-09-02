package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.dto.bulkupload.JobStatus;
import train.local.fogpass.entity.UploadJob;

import java.util.UUID;

@Repository
public interface UploadJobRepository extends JpaRepository<UploadJob, UUID> {
    long countByStatus(JobStatus status);
}