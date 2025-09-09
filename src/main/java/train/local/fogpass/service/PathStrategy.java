package train.local.fogpass.service;

import org.springframework.stereotype.Component;
import train.local.fogpass.entity.Route;

@Component
public class PathStrategy {
    public String routeRelativePath(Route route, String routeCodeOverride) {
        String zone = route.getSection().getDivision().getZone().getZonename();
        String division = route.getSection().getDivision().getName();
        String section = route.getSection().getName();
        String routeCode = routeCodeOverride != null ? routeCodeOverride : route.getRouteCode();
        return String.join("/", "zones", zone, division, section, routeCode);
    }
}