package train.local.fogpass.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.dto.masterdata.SectionCreateRequest;
import train.local.fogpass.dto.masterdata.SectionResponse;
import train.local.fogpass.dto.masterdata.SectionUpdateRequest;
import train.local.fogpass.entity.Division;
import train.local.fogpass.entity.Section;
import train.local.fogpass.entity.enums.SectionStatus;
import train.local.fogpass.exception.DuplicateSectionException;
import train.local.fogpass.exception.SectionNotFoundException;
import train.local.fogpass.exception.ResourceNotFoundException;
import train.local.fogpass.mapper.SectionMapper;
import train.local.fogpass.repository.DivisionRepository;
import train.local.fogpass.repository.SectionRepository;
import train.local.fogpass.security.SecurityUtil;
import train.local.fogpass.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import train.local.fogpass.service.SectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final DivisionRepository divisionRepository;
    private final SectionMapper sectionMapper;

    @Override
    public SectionResponse createSection(SectionCreateRequest request) {
        log.info("Creating section '{}' in division {}", request.getName(), request.getDivisionId());

        // Validate division exists
        Division division = divisionRepository.findById(request.getDivisionId())
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with ID: " + request.getDivisionId()));

        // Authorization: SUPER_ADMIN, or ZONE_ADMIN of parent zone, or DIVISION_ADMIN of this division
        ensureCanManageDivision(request.getDivisionId());

        // Uniqueness check within division
        if (sectionRepository.existsByNameAndDivisionId(request.getName(), request.getDivisionId())) {
            throw new DuplicateSectionException(request.getDivisionId(), request.getName());
        }

        Section entity = sectionMapper.toEntity(request);
        entity.setDivision(division);

        Section saved = sectionRepository.save(entity);
        return sectionMapper.toDto(saved);
    }

    @Override
    public SectionResponse updateSection(Long id, SectionUpdateRequest request) {
        log.info("Updating section id={}", id);

        Section existing = sectionRepository.findByIdWithDivision(id)
                .orElseThrow(() -> new SectionNotFoundException(id));

        // Authorization scoped to division of the existing section
        Long divisionId = existing.getDivision().getId();
        ensureCanManageDivision(divisionId);

        List<String> changed = new ArrayList<>();

        // Handle name change with uniqueness validation
        if (request.getName() != null && !request.getName().equals(existing.getName())) {
            if (sectionRepository.existsByNameAndDivisionIdAndIdNot(request.getName(), divisionId, id)) {
                throw new DuplicateSectionException(divisionId, request.getName());
            }
            existing.setName(request.getName());
            changed.add("name");
        }

        // Handle status change
        if (request.getStatus() != null) {
            SectionStatus newStatus = SectionStatus.valueOf(request.getStatus());
            if (existing.getStatus() != newStatus) {
                existing.setStatus(newStatus);
                changed.add("status");
            }
        }

        // createdAt/createdBy are immutable via JPA annotations; we don't touch them.
        existing.setUpdatedFields(changed.isEmpty() ? null : String.join(",", changed));

        Section updated = sectionRepository.save(existing);
        return sectionMapper.toDto(updated);
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getSectionsByDivision(Long divisionId) {
        // Validate division exists
        Division division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with ID: " + divisionId));
        // Scope-aware read: SUPER_ADMIN, ZONE_ADMIN of parent zone, or DIVISION_ADMIN of this division
        ensureCanManageDivision(divisionId);
        List<Section> sections = sectionRepository.findByDivisionId(divisionId);
        return sections.stream().map(sectionMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SectionResponse getSectionById(Long id) {
        Section section = sectionRepository.findByIdWithDivision(id)
                .orElseThrow(() -> new SectionNotFoundException(id));
        // Scope-aware read: SUPER_ADMIN or ZONE/DIVISION admin of its division
        ensureCanManageDivision(section.getDivision().getId());
        return sectionMapper.toDto(section);
    }

    @Override
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException(id));
        // Optional: enforce same authorization as update
        ensureCanManageDivision(section.getDivision().getId());
        sectionRepository.delete(section);
    }

    private void ensureCanManageDivision(Long divisionId) {
        UserPrincipal up = SecurityUtil.getCurrentUserPrincipal()
                .orElseThrow(() -> new AccessDeniedException("Unauthenticated user"));

        // SUPER_ADMIN: full access
        if (up.getRoleNames().contains("SUPER_ADMIN")) return;

        // DIVISION_ADMIN: must have scope for this division
        boolean divisionAllowed = up.getAccessScopes().stream().anyMatch(s ->
                "DIVISION_ADMIN".equals(s.getRoleName()) && s.getDivisionId() != null && s.getDivisionId().equals(divisionId)
        );
        if (divisionAllowed) return;

        // ZONE_ADMIN: must have scope for the parent zone of this division
        Division div = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with ID: " + divisionId));
        Long zoneId = div.getZone().getId();
        boolean zoneAllowed = up.getAccessScopes().stream().anyMatch(s ->
                "ZONE_ADMIN".equals(s.getRoleName()) && s.getZoneId() != null && s.getZoneId().equals(zoneId)
        );
        if (zoneAllowed) return;

        log.warn("ACCESS DENIED (Division): user={} roles={} requestedDivisionId={} (parentZone={})",
                up.getUsername(), up.getRoleNames(), divisionId, zoneId);
        throw new AccessDeniedException("Access denied for division: " + divisionId);
    }
}
