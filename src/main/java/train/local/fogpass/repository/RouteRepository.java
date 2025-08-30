package train.local.fogpass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import train.local.fogpass.entity.Route;
import train.local.fogpass.entity.enums.RouteStatus;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findBySectionIdAndStatus(Long sectionId, RouteStatus status);
    boolean existsByRouteCode(String routeCode);
}