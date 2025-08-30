package train.local.fogpass.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.audit.AuditAction;
import train.local.fogpass.audit.Auditable;
import train.local.fogpass.dto.masterdata.RouteCreateRequest;
import train.local.fogpass.dto.masterdata.RouteResponse;
import train.local.fogpass.dto.masterdata.RouteUpdateRequest;
import train.local.fogpass.entity.Route;
import train.local.fogpass.entity.Section;
import train.local.fogpass.entity.enums.RouteStatus;
import train.local.fogpass.exception.DuplicateResourceException;
import train.local.fogpass.exception.ResourceNotFoundException;
import train.local.fogpass.mapper.RouteMapper;
import train.local.fogpass.repository.RouteRepository;
import train.local.fogpass.repository.SectionRepository;
import train.local.fogpass.service.RouteService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final SectionRepository sectionRepository;
    private final RouteMapper routeMapper;

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, entityType = "Route")
    public RouteResponse createRoute(RouteCreateRequest request) {
        log.info("Creating route with code={} in section {}", request.getRouteCode(), request.getSectionId());

        // Validate section exists
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + request.getSectionId()));

        // Ensure unique routeCode
        if (routeRepository.existsByRouteCode(request.getRouteCode())) {
            throw new DuplicateResourceException("Route code already exists: " + request.getRouteCode());
        }

        Route entity = routeMapper.toEntity(request);
        entity.setSection(section);

        Route saved = routeRepository.save(entity);
        return routeMapper.toDto(saved);
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityType = "Route")
    public RouteResponse updateRoute(Long id, RouteUpdateRequest request) {
        log.info("Updating route id={}", id);
        Route existing = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with ID: " + id));

        // routeCode is immutable by mapper config; section is not changed here
        routeMapper.updateEntityFromDto(request, existing);

        Route updated = routeRepository.save(existing);
        return routeMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with ID: " + id));
        return routeMapper.toDto(route);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteResponse> getRoutesBySection(Long sectionId) {
        // validate section exists to avoid leaking info
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + sectionId));

        return routeRepository.findBySectionIdAndStatus(sectionId, RouteStatus.ACTIVE)
                .stream()
                .map(routeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entityType = "Route")
    public void updateRouteStatus(Long id, RouteStatus status) {
        log.info("Updating route status id={} -> {}", id, status);
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with ID: " + id));
        route.setStatus(status);
        routeRepository.save(route);
    }
}