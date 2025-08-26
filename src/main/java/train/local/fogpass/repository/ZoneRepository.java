package train.local.fogpass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.Zone;

import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    
    Optional<Zone> findByName(String name);
    
    Optional<Zone> findByCode(String code);
    
    boolean existsByName(String name);
    
    boolean existsByCode(String code);
    
    @Query("SELECT z FROM Zone z WHERE " +
           "(:name IS NULL OR LOWER(z.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(z.code) LIKE LOWER(CONCAT('%', :code, '%')))")
    Page<Zone> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
            @Param("name") String name, 
            @Param("code") String code, 
            Pageable pageable);
}