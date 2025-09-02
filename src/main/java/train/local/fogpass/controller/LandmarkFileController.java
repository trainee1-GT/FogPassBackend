package train.local.fogpass.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import train.local.fogpass.entity.LandmarkFile;
import train.local.fogpass.security.RoleConstants;
import train.local.fogpass.service.LandmarkFileService;

import java.io.IOException;

@RestController
@RequestMapping("/api/masterdata/landmark-files")
@PreAuthorize("hasAnyRole('" + RoleConstants.ADMIN + "','" + RoleConstants.SUPER_ADMIN + "')")
@RequiredArgsConstructor
public class LandmarkFileController {

    private final LandmarkFileService service;

    @PostMapping("/upload")
    public ResponseEntity<LandmarkFile> uploadFile(@RequestParam MultipartFile file,
                                                   @RequestParam Long routeId) throws IOException {
        LandmarkFile saved = service.uploadFile(file, routeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/by-route/{routeId}")
    public ResponseEntity<Page<LandmarkFile>> getFilesByRoute(@PathVariable Long routeId,
                                                              Pageable pageable) {
        return ResponseEntity.ok(service.getFilesByRoute(routeId, pageable));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        Resource resource = service.downloadFile(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('" + RoleConstants.SUPER_ADMIN + "')")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) throws IOException {
        service.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}