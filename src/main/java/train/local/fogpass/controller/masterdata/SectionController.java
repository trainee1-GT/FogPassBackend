package train.local.fogpass.controller.masterdata;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import train.local.fogpass.dto.masterdata.SectionCreateRequest;
import train.local.fogpass.dto.masterdata.SectionResponse;
import train.local.fogpass.dto.masterdata.SectionUpdateRequest;
import train.local.fogpass.service.SectionService;

import java.util.List;

@RestController
@RequestMapping("/api/masterdata/sections")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ZONE_ADMIN', 'DIVISION_ADMIN')")
public class SectionController {

    private final SectionService sectionService;

    @PostMapping
    public ResponseEntity<SectionResponse> createSection(@Valid @RequestBody SectionCreateRequest request) {
        SectionResponse response = sectionService.createSection(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<SectionResponse> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody SectionUpdateRequest request) {
        SectionResponse response = sectionService.updateSection(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-division/{divisionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SectionResponse>> getSectionsByDivision(
            @PathVariable Long divisionId) {
        List<SectionResponse> sections = sectionService.getSectionsByDivision(divisionId);
        return ResponseEntity.ok(sections);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SectionResponse> getSectionById(@PathVariable Long id) {
        SectionResponse section = sectionService.getSectionById(id);
        return ResponseEntity.ok(section);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}
