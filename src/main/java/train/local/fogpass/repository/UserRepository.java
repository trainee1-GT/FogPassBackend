package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
