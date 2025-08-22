package train.local.fogpass.service;

import train.local.fogpass.entity.Zone;

import java.util.List;
import java.util.Optional;

public interface ZoneService {
    List<Zone> getAllZones();
    Optional<Zone> getZoneById(Long id);
    Zone createZone(Zone zone);
    Optional<Zone> updateZone(Long id, Zone zone);
    boolean deleteZone(Long id);

    // ✅ Add this for bulk insert
    List<Zone> createZones(List<Zone> zones);
}