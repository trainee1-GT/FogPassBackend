package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.Landmark;

@Repository
public interface LandmarkRepository extends JpaRepository<Landmark, Long> {
    boolean existsByRoute_IdAndSequenceOrder(Long routeId, Integer sequenceOrder);
}
