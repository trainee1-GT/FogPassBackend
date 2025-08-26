package train.local.fogpass.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.dto.masterdata.ZoneCreateRequest;
import train.local.fogpass.dto.masterdata.ZoneResponse;
import train.local.fogpass.dto.masterdata.ZoneUpdateRequest;
import train.local.fogpass.dto.response.PageResponse;
import train.local.fogpass.entity.Zone;
import train.local.fogpass.exception.BadRequestException;
import train.local.fogpass.exception.ResourceNotFoundException;
import train.local.fogpass.mapper.ZoneMapper;
import train.local.fogpass.repository.ZoneRepository;
import train.local.fogpass.service.ZoneService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final ZoneMapper zoneMapper;

    @Override
    @Transactional
    public ZoneResponse createZone(ZoneCreateRequest request) {
        log.info("Creating new zone with name: {}", request.getName());
        
        // Validate uniqueness
        if (zoneRepository.existsByName(request.getName())) {
            throw new BadRequestException("Zone with name '" + request.getName() + "' already exists");
        }
        
        if (zoneRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Zone with code '" + request.getCode() + "' already exists");
        }
        
        Zone zone = zoneMapper.toEntity(request);
        Zone savedZone = zoneRepository.save(zone);
        
        log.info("Zone created successfully with ID: {}", savedZone.getId());
        return zoneMapper.toResponse(savedZone);
    }

    @Override
    @Transactional(readOnly = true)
    public ZoneResponse getZoneById(Long id) {
        log.debug("Fetching zone with ID: {}", id);
        
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + id));
        
        return zoneMapper.toResponse(zone);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZoneResponse> getAllZones() {
        log.debug("Fetching all zones");
        
        List<Zone> zones = zoneRepository.findAll();
        return zoneMapper.toResponseList(zones);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ZoneResponse> getAllZones(Pageable pageable) {
        log.debug("Fetching zones with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Zone> zonePage = zoneRepository.findAll(pageable);
        return zoneMapper.toPageResponse(zonePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ZoneResponse> searchZones(String name, String code, Pageable pageable) {
        log.debug("Searching zones with filters - name: {}, code: {}", name, code);
        
        Page<Zone> zonePage = zoneRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                name, code, pageable);
        return zoneMapper.toPageResponse(zonePage);
    }

    @Override
    @Transactional
    public ZoneResponse updateZone(Long id, ZoneUpdateRequest request) {
        log.info("Updating zone with ID: {}", id);
        
        Zone existingZone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + id));
        
        // Validate uniqueness for updated fields
        if (request.getName() != null && !request.getName().equals(existingZone.getName())) {
            if (zoneRepository.existsByName(request.getName())) {
                throw new BadRequestException("Zone with name '" + request.getName() + "' already exists");
            }
        }
        
        if (request.getCode() != null && !request.getCode().equals(existingZone.getCode())) {
            if (zoneRepository.existsByCode(request.getCode())) {
                throw new BadRequestException("Zone with code '" + request.getCode() + "' already exists");
            }
        }
        
        // Apply partial updates
        zoneMapper.updateEntityFromRequest(request, existingZone);
        Zone updatedZone = zoneRepository.save(existingZone);
        
        log.info("Zone updated successfully with ID: {}", updatedZone.getId());
        return zoneMapper.toResponse(updatedZone);
    }

    @Override
    @Transactional
    public void deleteZone(Long id) {
        log.info("Deleting zone with ID: {}", id);
        
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with ID: " + id));
        
        // Check if zone has divisions
        if (!zone.getDivisions().isEmpty()) {
            throw new BadRequestException("Cannot delete zone with existing divisions. Delete divisions first.");
        }
        
        zoneRepository.delete(zone);
        log.info("Zone deleted successfully with ID: {}", id);
    }
}