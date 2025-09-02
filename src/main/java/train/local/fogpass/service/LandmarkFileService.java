package train.local.fogpass.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import train.local.fogpass.audit.AuditAction;
import train.local.fogpass.audit.Auditable;
import train.local.fogpass.entity.LandmarkFile;
import train.local.fogpass.entity.Route;
import train.local.fogpass.exception.BadRequestException;
import train.local.fogpass.exception.ResourceNotFoundException;
import train.local.fogpass.repository.LandmarkFileRepository;
import train.local.fogpass.repository.RouteRepository;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LandmarkFileService {

    private final LandmarkFileRepository repository;
    private final RouteRepository routeRepository;
    private final FileStorageService fileStorageService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Auditable(action = AuditAction.FILE_UPLOAD)
    public LandmarkFile uploadFile(MultipartFile file, Long routeId) throws IOException {
        validateFile(file);

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + routeId));

        String storageFileName = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        String fullPath = fileStorageService.storeFile(file, routeId, storageFileName);

        LandmarkFile lf = new LandmarkFile(
                file.getOriginalFilename(),
                storageFileName,
                fullPath,
                file.getContentType(),
                file.getSize(),
                route
        );

        return repository.save(lf);
    }

    @Transactional
    @Auditable(action = AuditAction.FILE_DELETE)
    public void deleteFile(Long fileId) throws IOException {
        LandmarkFile file = repository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        String fullFilePath = file.getFilePath();

        repository.delete(file); // hard delete DB row
        fileStorageService.deleteFile(fullFilePath); // hard delete disk file
    }

    @Auditable(action = AuditAction.FILE_DOWNLOAD)
    public Resource downloadFile(Long fileId) throws IOException {
        LandmarkFile file = repository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        return fileStorageService.loadFileAsResource(file.getFilePath());
    }

    public Page<LandmarkFile> getFilesByRoute(Long routeId, Pageable pageable) {
        return repository.findByRoute_Id(routeId, pageable);
    }

    private void validateFile(MultipartFile file) {
        String ct = file.getContentType();
        String name = file.getOriginalFilename();
        String ext = getExtension(name).toLowerCase(Locale.ROOT);
        boolean extOk = ".csv".equals(ext) || ".xls".equals(ext) || ".xlsx".equals(ext);
        boolean ctOk = ct != null && (
                ct.equals("text/csv") ||
                ct.equals("application/vnd.ms-excel") ||
                ct.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                ct.equals("application/octet-stream") ||
                ct.equals("text/plain")
        );
        if (!extOk && !ctOk) {
            throw new BadRequestException("Invalid file type: " + ct + " ext=" + ext);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx) : "";
    }
}