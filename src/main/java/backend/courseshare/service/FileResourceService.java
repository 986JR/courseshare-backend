package backend.courseshare.service;

import backend.courseshare.dto.fileresource.FileResourceListResponse;
import backend.courseshare.dto.fileresource.FileResourceResponse;
import backend.courseshare.dto.fileresource.UploadFileResourceRequest;
import backend.courseshare.entity.Course;
import backend.courseshare.entity.Enrollment;
import backend.courseshare.entity.FileResource;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.CourseRepository;
import backend.courseshare.repository.EnrollmentRepository;
import backend.courseshare.repository.FileResourceRepository;
import backend.courseshare.storage.FileStorageService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileResourceService {

    private final FileResourceRepository fileResourceRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FileStorageService storageService;
    private final EmailService emailService;

    public FileResourceService(
            FileResourceRepository fileResourceRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            FileStorageService storageService,
            EmailService emailService
    ) {
        this.fileResourceRepository = fileResourceRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.storageService = storageService;
        this.emailService = emailService;
    }

    public FileResourceResponse upload(
            MultipartFile file,
            UploadFileResourceRequest request,
            Users uploader
    ) throws IOException {

        Course course = courseRepository.findByCourseCode(request.courseCode())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        boolean isAdmin = uploader.getRole().equals("ADMIN");

        boolean isEnrolled = enrollmentRepository
                .existsByUser_IdAndCourse_Id(uploader.getId(), course.getId());

        if (!isAdmin && !isEnrolled) {
            throw new IllegalArgumentException("You must be enrolled to upload");
        }

        // Save file to disk
        String fileUrl = storageService.saveFile(file, course.getCourseCode());

        // Map to entity
        FileResource fr = new FileResource();
        fr.setFilename(file.getOriginalFilename());
        fr.setFileType(file.getContentType());
        fr.setSize(file.getSize());
        fr.setDescription(request.description());
        fr.setFileUrl(fileUrl);
        fr.setUploadedBy(uploader);
        fr.setCourse(course);

        fileResourceRepository.save(fr);
        /*String body = "Thank You Very much for Contributing to CourseSahere, Welcome Back, And Upload more";
        emailService.sendEmail(uploader.getEmail(),"Thank You For uploading",body);*/
        return toResponse(fr);
    }

    public FileResourceListResponse getFilesByCourse(String code, int page, int size) {
        Page<FileResource> result = fileResourceRepository.findByCourse_CourseCode(
                code,
                PageRequest.of(page, size)
        );

        return new FileResourceListResponse(
                result.stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }



    private FileResourceResponse toResponse(FileResource f) {
        return new FileResourceResponse(
                f.getPublicId(),
                f.getFilename(),
                f.getFileType(),
                f.getDescription(),
                f.getFileUrl(),
                f.getUploadedBy().getUsername(),
                f.getCourse().getCourseCode(),
                f.getUploadedAt().toString(),
                f.getSize()
        );
    }

    public FileResourceListResponse searchFiles(String query, int page, int size) {
        System.out.println("SEARCH QUERY = " + query);

        Page<FileResource> result = fileResourceRepository.searchFiles(query, PageRequest.of(page, size));

        return  new FileResourceListResponse(
                result.stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    public FileResourceListResponse listMyUploads(String userPublicId, int page, int size) {

        Page<FileResource> result =
                fileResourceRepository.findByUploadedBy_PublicIdOrderByUploadedAtDesc(
                        userPublicId, PageRequest.of(page, size)
                );

        return new FileResourceListResponse(
                result.stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }



}
