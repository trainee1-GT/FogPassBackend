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
        
        return divisionMapper.toResponse(division);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DivisionResponse> getAllDivisions() {
        log.debug("Fetching all divisions");
        
        List<Division> divisions = divisionRepository.findAll();
        return divisionMapper.toResponseList(divisions);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DivisionResponse> getAllDivisions(Pageable pageable) {
        log.debug("Fetching divisions with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Division> divisionPage = divisionRepository.findAll(pageable);
        return divisionMapper.toPageResponse(divisionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DivisionResponse> getDivisionsByZone(Long zoneId) {
        log.debug("Fetching divisions for zone: {}", zoneId);
        
        // Validate zone exists
        if (!zoneRepository.existsById(zoneId)) {
            throw new ResourceNotFoundException("Zone not found with ID: " + zoneId);
        }
        
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
        
        Page<Division> divisionPage = divisionRepository.findByZoneId(zoneId, pageable);
        return divisionMapper.toPageResponse(divisionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DivisionResponse> searchDivisions(Long zoneId, String name, String code, Pageable pageable) {
        log.debug("Searching divisions with filters - zoneId: {}, name: {}, code: {}", zoneId, name, code);
        
        Page<Division> divisionPage = divisionRepository.findByFilters(zoneId, name, code, pageable);
        return divisionMapper.toPageResponse(divisionPage);
    }

    @Override
    @Transactional
    public DivisionResponse updateDivision(Long id, DivisionUpdateRequest request) {
        log.info("Updating division with ID: {}", id);
        
        Division existingDivision = divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with ID: " + id));
        
        // Handle zone change if provided
        if (request.getZoneId() != null && !request.getZoneId().equals(existingDivision.getZone().getId())) {
            Zone newZone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + request.getZoneId()));
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
        
        // Check if division has sections
        if (!division.getSections().isEmpty()) {
            throw new BadRequestException("Cannot delete division with existing sections. Delete sections first.");
        }
        
        divisionRepository.delete(division);
        log.info("Division deleted successfully with ID: {}", id);
    }
}