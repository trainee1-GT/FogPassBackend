package train.local.fogpass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.LandmarkFile;

@Repository
public interface LandmarkFileRepository extends JpaRepository<LandmarkFile, Long> {
    Page<LandmarkFile> findByRoute_Id(Long routeId, Pageable pageable);
}