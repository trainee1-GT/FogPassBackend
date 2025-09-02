package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
}
