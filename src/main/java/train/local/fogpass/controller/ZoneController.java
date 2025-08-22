package train.local.fogpass.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import train.local.fogpass.entity.Zone;
import train.local.fogpass.service.ZoneService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    // GET all zones
    @GetMapping
    public ResponseEntity<List<Zone>> getAll() {
        return ResponseEntity.ok(zoneService.getAllZones());
    }

    // GET zone by ID
    @GetMapping("/{id}")
    public ResponseEntity<Zone> getById(@PathVariable("id") Long id) {
        return zoneService.getZoneById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // CREATE zone
    @PostMapping("/bulk")
    public ResponseEntity<List<Zone>> createBulk(@RequestBody List<Zone> payloads) {
        List<Zone> savedZones = zoneService.createZones(payloads);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedZones);
    }

    // UPDATE zone
    @PutMapping("id/{id}")
    public ResponseEntity<Zone> update(@PathVariable("id") Long id, @RequestBody Zone payload) {
        return zoneService.updateZone(id, payload)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // DELETE zone
    @DeleteMapping("id/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        boolean deleted = zoneService.deleteZone(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}