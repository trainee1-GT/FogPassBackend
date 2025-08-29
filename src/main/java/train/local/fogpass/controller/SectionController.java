package train.local.fogpass.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import train.local.fogpass.dto.request.SectionBulkRequest;
import train.local.fogpass.entity.Section;
import train.local.fogpass.service.SectionService;

import java.util.List;

@RestController
@RequestMapping({"/sections", "/api/sections"})
@PreAuthorize("hasRole(T(train.local.fogpass.security.RoleConstants).SUPER_ADMIN)")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    // Get all sections
    @GetMapping
    public ResponseEntity<List<Section>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }

    // Get sections by division
    @GetMapping("/division/{divisionId}")
    public ResponseEntity<List<Section>> getSectionsByDivision(@PathVariable Long divisionId) {
        return ResponseEntity.ok(sectionService.getSectionsByDivision(divisionId));
    }

    // Create one section (accepts { name, divisionId })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Section> createSection(@Valid @RequestBody Section section) {
        Section saved = sectionService.saveSection(section);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Bulk create sections with array of Section objects
    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Section>> createSections(@Valid @RequestBody List<@Valid Section> sections) {
        List<Section> saved = sectionService.saveAllSections(sections);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Bulk create by divisionId + names list
    @PostMapping(value = "/bulk-dto", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Section>> createSectionsDto(@RequestBody SectionBulkRequest req) {
        List<Section> payload = req.getSections().stream().map(name -> {
            Section s = new Section();
            s.setName(name);
            s.setDivisionId(req.getDivisionId());
            return s;
        }).toList();
        List<Section> saved = sectionService.saveAllSections(payload);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Delete a section
    @DeleteMapping({"/{id}", "/id/{id}"})
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}