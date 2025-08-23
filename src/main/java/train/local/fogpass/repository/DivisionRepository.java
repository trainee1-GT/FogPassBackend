package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import train.local.fogpass.entity.Division;
import java.util.List;

public interface DivisionRepository extends JpaRepository<Division, Long> {
    // Custom finder: get all divisions under a zone (via Zone.zoneId field)
    List<Division> findByZoneZoneId(Long zoneId);
}
