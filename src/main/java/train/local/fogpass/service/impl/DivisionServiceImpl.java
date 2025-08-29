package train.local.fogpass.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.dto.masterdata.DivisionCreateRequest;
import train.local.fogpass.dto.masterdata.DivisionResponse;
import train.local.fogpass.dto.masterdata.DivisionUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;
import train.local.fogpass.entity.Division;
import train.local.fogpass.entity.Zone;
import train.local.fogpass.exception.BadRequestException;
import train.local.fogpass.exception.ResourceNotFoundException;
import train.local.fogpass.mapper.DivisionMapper;
import train.local.fogpass.repository.DivisionRepository;
import train.local.fogpass.repository.ZoneRepository;
import train.local.fogpass.service.DivisionService;
import train.local.fogpass.security.SecurityUtil;
import train.local.fogpass.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import java.util.Objects;
import java.util.stream.Collectors;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;
    private final ZoneRepository zoneRepository;
    private final DivisionMapper divisionMapper;

    @Override
    @Transactional
    public DivisionResponse createDivision(DivisionCreateRequest request) {
        log.info("Creating new division with name: {} in zone: {}", request.getName(), request.getZoneId());
        
        // Validate zone exists
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + request.getZoneId()));

        // Scope enforcement
        ensureCanManageZone(zone.getId());
        
        // Validate uniqueness within zone
        if (divisionRepository.existsByNameAndZoneId(request.getName(), request.getZoneId())) {
            throw new BadRequestException("Division with name '" + request.getName() + "' already exists in this zone");
        }
        
        if (divisionRepository.existsByCodeAndZoneId(request.getCode(), request.getZoneId())) {
            throw new BadRequestException("Division with code '" + request.getCode() + "' already exists in this zone");
        }
        
        Division division = divisionMapper.toEntity(request);
        division.setZone(zone);
        Division savedDivision = divisionRepository.save(division);
        
        log.info("Division created successfully with ID: {}", savedDivision.getId());
        return divisionMapper.toResponse(savedDivision);
    }

    @Override
    @Transactional(readOnly = true)
    public DivisionResponse getDivisionById(Long id) {
        log.debug("Fetching division with ID: {}", id);
        
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with ID: " + id));

        // Scope-aware read: only SUPER_ADMIN or ZONE_ADMIN of the division's zone
        ensureCanManageZone(division.getZone().getId());
        
        return divisionMapper.toResponse(division);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DivisionResponse> getAllDivisions() {
        log.debug("Fetching all divisions (scope-aware)");

        UserPrincipal up = SecurityUtil.getCurrentUserPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Unauthenticated user"));

        if (up.getRoleNames().contains("SUPER_ADMIN")) {
            List<Division> divisions = divisionRepository.findAll();
            return divisionMapper.toResponseList(divisions);
        }

        // For ZONE_ADMIN: aggregate over assigned zones
        List<Long> zoneIds = up.getAccessScopes().stream()
                .filter(s -> "ZONE_ADMIN".equals(s.getRoleName()))
                .map(UserPrincipal.ScopeView::getZoneId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (zoneIds.isEmpty()) {
            // No zone scope -> no data
            return List.of();
        }

        // Fetch per zone and flatten
        List<Division> divisions = zoneIds.stream()
                .flatMap(zid -> divisionRepository.findByZoneId(zid).stream())
                .collect(Collectors.toList());
        return divisionMapper.toResponseList(divisions);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DivisionResponse> getAllDivisions(Pageable pageable) {
        log.debug("Fetching divisions with pagination (scope-aware): page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        UserPrincipal up = SecurityUtil.getCurrentUserPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Unauthenticated user"));

        if (up.getRoleNames().contains("SUPER_ADMIN")) {
            Page<Division> divisionPage = divisionRepository.findAll(pageable);
            return divisionMapper.toPageResponse(divisionPage);
        }

        // For ZONE_ADMIN: since repo lacks IN query paged, filter per zone then page in memory (simple approach)
        List<DivisionResponse> all = getAllDivisions(); // already scope-filtered
        return toPageResponseFromList(all, pageable);
    }

    private PageResponse<DivisionResponse> toPageResponseFromList(List<DivisionResponse> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<DivisionResponse> slice = start > end ? List.of() : all.subList(start, end);

        PageResponse<DivisionResponse> resp = new PageResponse<>();
        resp.setContent(slice);
        resp.setPageNumber(pageable.getPageNumber());
        resp.setPageSize(pageable.getPageSize());
        resp.setTotalElements((long) all.size());
        resp.setTotalPages((int) Math.ceil(all.size() / (double) pageable.getPageSize()));
        resp.setFirst(pageable.getPageNumber() == 0);
        resp.setLast(end >= all.size());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DivisionResponse> getDivisionsByZone(Long zoneId) {
        log.debug("Fetching divisions for zone: {}", zoneId);
        
        // Validate zone exists
        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("Zone not found with ID: " + zoneId);
        }

        // Scope-aware read: ensure caller can access this zone
        ensureCanManageZone(zoneId);
        
        List<Division> divisions = divisionRepository.findByZoneId(zoneId);
        return divisionMapper.toResponseList(divisions);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DivisionResponse> getDivisionsByZone(Long zoneId, Pageable pageable) {
        log.debug("Fetching divisions for zone: {} with pagination", zoneId);
        
        // Validate zone exists
        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("Zone not found with ID: " + zoneId);
        }

        // Scope-aware read: ensure caller can access this zone
        ensureCanManageZone(zoneId);
        
        Page<Division> divisionPage = divisionRepository.findByZoneId(zoneId, pageable);
        return divisionMapper.toPageResponse(divisionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DivisionResponse> searchDivisions(Long zoneId, String name, String code, Pageable pageable) {
        log.debug("Searching divisions with filters (scope-aware) - zoneId: {}, name: {}, code: {}", zoneId, name, code);

        UserPrincipal up = SecurityUtil.getCurrentUserPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Unauthenticated user"));

        Long effectiveZoneId = zoneId;
        if (!up.getRoleNames().contains("SUPER_ADMIN")) {
            // If caller is ZONE_ADMIN, force the zone filter to one of their zones
            List<Long> zoneIds = up.getAccessScopes().stream()
                    .filter(s -> "ZONE_ADMIN".equals(s.getRoleName()))
                    .map(UserPrincipal.ScopeView::getZoneId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (zoneIds.isEmpty()) {
                return divisionMapper.toPageResponse(Page.empty(pageable));
            }
            // If client passed a zoneId not in their scope -> deny
            if (effectiveZoneId != null && !zoneIds.contains(effectiveZoneId)) {
                log.warn("Access denied: user={} roles={} attempted search in zoneId={} not in scope {}",
                        up.getUsername(), up.getRoleNames(), effectiveZoneId, zoneIds);
                throw new AccessDeniedException("Access denied for zone: " + effectiveZoneId);
            }
            // If no zoneId provided, limit to their first zone (or we could return aggregated)
            if (effectiveZoneId == null) {
                // Simple: pick first zone to maintain pageable semantics
                effectiveZoneId = zoneIds.get(0);
            }
        }

        Page<Division> divisionPage = divisionRepository.findByFilters(effectiveZoneId, name, code, pageable);
        return divisionMapper.toPageResponse(divisionPage);
    }

    @Override
    @Transactional
    public DivisionResponse updateDivision(Long id, DivisionUpdateRequest request) {
        log.info("Updating division with ID: {}", id);
        
        Division existingDivision = divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with ID: " + id));

        // Scope enforcement on current zone
        ensureCanManageZone(existingDivision.getZone().getId());
        
        // Handle zone change if provided
        if (request.getZoneId() != null && !request.getZoneId().equals(existingDivision.getZone().getId())) {
            Zone newZone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + request.getZoneId()));
            // Also ensure caller can manage the new zone
            ensureCanManageZone(newZone.getId());
            existingDivision.setZone(newZone);
        }
        
        Long zoneId = existingDivision.getZone().getId();
        
        // Validate uniqueness for updated fields within the zone
        if (request.getName() != null && !request.getName().equals(existingDivision.getName())) {
            if (divisionRepository.existsByNameAndZoneId(request.getName(), zoneId)) {
                throw new BadRequestException("Division with name '" + request.getName() + "' already exists in this zone");
            }
        }
        
        if (request.getCode() != null && !request.getCode().equals(existingDivision.getCode())) {
            if (divisionRepository.existsByCodeAndZoneId(request.getCode(), zoneId)) {
                throw new BadRequestException("Division with code '" + request.getCode() + "' already exists in this zone");
            }
        }
        
        // Apply partial updates
        divisionMapper.updateEntityFromRequest(request, existingDivision);
        Division updatedDivision = divisionRepository.save(existingDivision);
        
        log.info("Division updated successfully with ID: {}", updatedDivision.getId());
        return divisionMapper.toResponse(updatedDivision);
    }

    @Override
    @Transactional
    public void deleteDivision(Long id) {
        log.info("Deleting division with ID: {}", id);
        
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with ID: " + id));

        // Scope enforcement
        ensureCanManageZone(division.getZone().getId());
        
        // Check if division has sections
        if (!division.getSections().isEmpty()) {
            throw new BadRequestException("Cannot delete division with existing sections. Delete sections first.");
        }
        
        divisionRepository.delete(division);
        log.info("Division deleted successfully with ID: {}", id);
    }

    private void ensureCanManageZone(Long zoneId) {
        UserPrincipal up = SecurityUtil.getCurrentUserPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Unauthenticated user"));
        if (up.getRoleNames().contains("SUPER_ADMIN")) return;
        boolean allowed = up.getAccessScopes().stream().anyMatch(s ->
                "ZONE_ADMIN".equals(s.getRoleName()) && s.getZoneId() != null && s.getZoneId().equals(zoneId)
        );
        if (!allowed) {
            log.warn("ACCESS DENIED (Zone): user={} roles={} requestedZoneId={}", up.getUsername(), up.getRoleNames(), zoneId);
            throw new AccessDeniedException("Access denied for zone: " + zoneId);
        }
    }
}