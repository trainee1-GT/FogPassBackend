package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.Section;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Section} entities.
 * Provides CRUD operations and custom queries for Section management.
 */
@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    /**
     * Find all sections by division ID
     */
    List<Section> findByDivisionId(Long divisionId);

    /**
     * Check if a section with the given name exists in the specified division
     */
    boolean existsByNameAndDivisionId(String name, Long divisionId);

    /**
     * Check if a section with the given name exists in the specified division, excluding the given section ID
     */
    boolean existsByNameAndDivisionIdAndIdNot(String name, Long divisionId, Long id);

    /**
     * Find a section by ID with its division eagerly loaded
     */
    @Query("SELECT s FROM Section s JOIN FETCH s.division d WHERE s.id = :id")
    Optional<Section> findByIdWithDivision(@Param("id") Long id);

    /**
     * Find a section by ID and division ID with its division eagerly loaded
     */
    @Query("SELECT s FROM Section s JOIN FETCH s.division d WHERE s.id = :id AND d.id = :divisionId")
    Optional<Section> findByIdAndDivisionId(@Param("id") Long id, @Param("divisionId") Long divisionId);
}