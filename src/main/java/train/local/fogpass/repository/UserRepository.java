package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"accessScopes", "accessScopes.role"})
    Optional<User> findByUsername(String username);

    @Override
    @EntityGraph(attributePaths = {"accessScopes", "accessScopes.role"})
    Optional<User> findById(Long aLong);

    @Override
    @EntityGraph(attributePaths = {"accessScopes", "accessScopes.role"})
    List<User> findAll();

    // For validations used in services
    boolean existsByUsername(String username);
    boolean existsByUserId(Long userId);
}