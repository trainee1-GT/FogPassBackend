package train.local.fogpass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "landmark_files", indexes = {
        @Index(name = "idx_landmark_files_route", columnList = "route_id")
})
public class LandmarkFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false, unique = true)
    private String storageFileName;

    @Column(nullable = false, length = 1024)
    private String filePath; // absolute path on disk

    private String fileType;

    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    public LandmarkFile(String originalFileName, String storageFileName, String filePath,
                        String fileType, Long fileSize, Route route) {
        this.originalFileName = originalFileName;
        this.storageFileName = storageFileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.route = route;
    }
}