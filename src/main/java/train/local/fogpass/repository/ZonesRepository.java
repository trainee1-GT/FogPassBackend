package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.Zone;

@Repository
public interface ZonesRepository extends JpaRepository<Zone, Long> {
    boolean existsByZonename(String zonename);
}