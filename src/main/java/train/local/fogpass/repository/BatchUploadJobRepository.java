package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import train.local.fogpass.entity.BatchUploadJob;
import java.util.UUID;

public interface BatchUploadJobRepository extends JpaRepository<BatchUploadJob, UUID> { }