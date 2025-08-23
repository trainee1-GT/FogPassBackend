package train.local.fogpass.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import train.local.fogpass.dto.request.DivisionRequest;
import train.local.fogpass.entity.Division;
import train.local.fogpass.entity.Zone;
import train.local.fogpass.service.DivisionService;

import java.util.List;

@RestController
@RequestMapping({"/divisions", "/api/divisions"})
public class DivisionController {

    private final DivisionService divisionService;

    public DivisionController(DivisionService divisionService) {
        this.divisionService = divisionService;
    }

    // ✅ Get all divisions
    @GetMapping
    public ResponseEntity<List<Division>> getAllDivisions() {
        return ResponseEntity.ok(divisionService.getAllDivisions());
    }

    // ✅ Get all divisions under a zone
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<Division>> getDivisionsByZone(@PathVariable Long zoneId) {
        return ResponseEntity.ok(divisionService.getDivisionsByZone(zoneId));
    }

    // ✅ Create a single division
    @PostMapping
    public ResponseEntity<Division> createDivision(@Valid @RequestBody Division division) {
        Division savedDivision = divisionService.saveDivision(division);
        return new ResponseEntity<>(savedDivision, HttpStatus.CREATED); // 201 CREATED
    }

    // ✅ Bulk insert divisions (all in one bunch)
    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Division>> createDivisions(@Valid @RequestBody List<@Valid Division> divisions) {
        List<Division> savedDivisions = divisionService.saveAllDivisions(divisions);
        return new ResponseEntity<>(savedDivisions, HttpStatus.CREATED); // 201 CREATED
    }

    // ✅ Bulk insert using flat DTO (name + zoneId)
    @PostMapping(value = "/bulk-dto", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Division>> createDivisionsDto(@RequestBody List<DivisionRequest> requests) {
        List<Division> divisions = requests.stream().map(r -> {
            Division d = new Division();
            d.setName(r.getName());
            Zone z = new Zone();
            z.setZoneId(r.getZoneId());
            d.setZone(z);
            return d;
        }).toList();
        List<Division> savedDivisions = divisionService.saveAllDivisions(divisions);
        return new ResponseEntity<>(savedDivisions, HttpStatus.CREATED);
    }

    // ✅ Get a specific division by ID
    @GetMapping({"/{id}", "/id/{id}"})
    public ResponseEntity<Division> getDivisionById(@PathVariable Long id) {
        Division division = divisionService.getDivisionById(id);
        if (division != null) {
            return ResponseEntity.ok(division); // 200 OK
        } else {
            return ResponseEntity.notFound().build(); // 404 NOT FOUND
        }
    }

    // ✅ Update an existing division
    @PutMapping({"/{id}", "/id/{id}"})
    public ResponseEntity<Division> updateDivision(@PathVariable Long id, @Valid @RequestBody Division updatedDivision) {
        updatedDivision.setId(id);
        Division savedDivision = divisionService.saveDivision(updatedDivision);
        return ResponseEntity.ok(savedDivision); // 200 OK
    }

    // ✅ Delete a division
    @DeleteMapping({"/{id}", "/id/{id}"})
    public ResponseEntity<Void> deleteDivision(@PathVariable Long id) {
        divisionService.deleteDivision(id);
        return ResponseEntity.noContent().build(); // 204 NO CONTENT
    }
}
