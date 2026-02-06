package backend.courseshare.entity;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="file_resources", indexes = {
        @Index(name="idx_file_resources_public_id", columnList = "public_id"),
        @Index(name="idx_file_resources_course_id", columnList = "course_id")
})
public class FileResource {


    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name= "public_id", nullable = false, unique = true, updatable = false)
    private String publicId = UUID.randomUUID().toString();

    @Column(name= "file_name", nullable = false, length = 255)
    private String filename;

    @Column(name="file_type", length = 100)
    private String fileType;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "description", length = 2500)
    private String description;

    @Column(name = "size")
    private Long size;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private Users uploadedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Users getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Users uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @PrePersist
    public  void onCreate() {
        if(uploadedAt == null) {
            uploadedAt = Instant.now();
        }


    }
}
