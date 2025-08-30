package train.local.fogpass.service;

import train.local.fogpass.dto.masterdata.RouteCreateRequest;
import train.local.fogpass.dto.masterdata.RouteResponse;
import train.local.fogpass.dto.masterdata.RouteUpdateRequest;
import train.local.fogpass.entity.enums.RouteStatus;

import java.util.List;

public interface RouteService {
    RouteResponse createRoute(RouteCreateRequest request);
    RouteResponse updateRoute(Long id, RouteUpdateRequest request);
    RouteResponse getRouteById(Long id);
    List<RouteResponse> getRoutesBySection(Long sectionId);
    void updateRouteStatus(Long id, RouteStatus status);
}