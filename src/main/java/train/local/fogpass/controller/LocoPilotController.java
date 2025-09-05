package train.local.fogpass.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import train.local.fogpass.dto.response.ApiResponse;
import train.local.fogpass.entity.Pilot;
import train.local.fogpass.service.PilotService;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loco")
public class LocoPilotController {

    private final PilotService pilotService;

    public LocoPilotController(PilotService pilotService) {
        this.pilotService = pilotService;
    }

    // 1) Sample JSON data (e.g., list of loco pilots)
    @GetMapping("/sample")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> samplePilots() {
        List<Map<String, Object>> data = List.of(
                Map.of("id", 1, "name", "John Doe", "email", "john.doe@example.com"),
                Map.of("id", 2, "name", "Jane Smith", "email", "jane.smith@example.com")
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Sample pilots", data));
    }

    // 3) CRUD for Pilot
    @GetMapping("/pilots")
    public ResponseEntity<ApiResponse<List<Pilot>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", pilotService.findAll()));
    }

    @GetMapping("/pilots/{id}")
    public ResponseEntity<ApiResponse<Pilot>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", pilotService.findById(id)));
    }

    @PostMapping("/pilots")
    public ResponseEntity<ApiResponse<Pilot>> create(@Valid @RequestBody Pilot pilot) {
        Pilot created = pilotService.create(pilot);
        return ResponseEntity.created(URI.create("/api/loco/pilots/" + created.getId()))
                .body(new ApiResponse<>(true, "Created", created));
    }

    @PutMapping("/pilots/{id}")
    public ResponseEntity<ApiResponse<Pilot>> update(@PathVariable Long id, @Valid @RequestBody Pilot pilot) {
        Pilot updated = pilotService.update(id, pilot);
        return ResponseEntity.ok(new ApiResponse<>(true, "Updated", updated));
    }

    @DeleteMapping("/pilots/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        pilotService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Deleted", null));
    }
}