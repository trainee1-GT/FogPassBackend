package train.local.fogpass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.Division;

import java.util.List;
import java.util.Optional;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Long> {
    
    List<Division> findByZoneId(Long zoneId);
    
    Page<Division> findByZoneId(Long zoneId, Pageable pageable);
    
    Optional<Division> findByNameAndZoneId(String name, Long zoneId);
    
    Optional<Division> findByCodeAndZoneId(String code, Long zoneId);
    
    boolean existsByNameAndZoneId(String name, Long zoneId);
    
    boolean existsByCodeAndZoneId(String code, Long zoneId);
    
    @Query("SELECT d FROM Division d WHERE " +
           "(:zoneId IS NULL OR d.zone.id = :zoneId) AND " +
           "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :code, '%')))")
    Page<Division> findByFilters(
            @Param("zoneId") Long zoneId,
            @Param("name") String name, 
            @Param("code") String code, 
            Pageable pageable);
}