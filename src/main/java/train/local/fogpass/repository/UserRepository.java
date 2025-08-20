package train.local.fogpass.repository;

import train.local.fogpass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // No JPQL here, only default methods like save(), findAll(), findById(), deleteById()
}
