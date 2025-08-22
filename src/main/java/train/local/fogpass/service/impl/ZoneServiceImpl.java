package train.local.fogpass.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.entity.Zone;
import train.local.fogpass.repository.ZonesRepository;
import train.local.fogpass.service.ZoneService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ZoneServiceImpl implements ZoneService {

    private final ZonesRepository zonesRepository;

    public ZoneServiceImpl(ZonesRepository zonesRepository) {
        this.zonesRepository = zonesRepository;
    }

    @Override
    public List<Zone> getAllZones() {
        return zonesRepository.findAll();
    }

    @Override
    public Optional<Zone> getZoneById(Long id) {
        return zonesRepository.findById(id);
    }

    @Override
    public Zone createZone(Zone zone) {
        Zone toSave = new Zone();
        toSave.setZonename(zone.getZonename());
        return zonesRepository.save(toSave);
    }

    @Override
    public Optional<Zone> updateZone(Long id, Zone zone) {
        return zonesRepository.findById(id)
                .map(existing -> {
                    existing.setZonename(zone.getZonename());
                    return zonesRepository.save(existing);
                });
    }

    @Override
    public boolean deleteZone(Long id) {
        if (!zonesRepository.existsById(id)) return false;
        zonesRepository.deleteById(id);
        return true;
    }

    // ✅ NEW: Bulk insert method
    @Override
    public List<Zone> createZones(List<Zone> zones) {
        List<Zone> toSave = zones.stream()
                .map(zone -> {
                    Zone z = new Zone();
                    z.setZonename(zone.getZonename());
                    return z;
                })
                .collect(Collectors.toList());

        return zonesRepository.saveAll(toSave);
    }
}
