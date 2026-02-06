package backend.courseshare.controller;


import backend.courseshare.dto.enrollment.EnrollmentResponse;
import backend.courseshare.dto.fileresource.FileResourceListResponse;
import backend.courseshare.dto.fileresource.FileResourceResponse;
import backend.courseshare.dto.fileresource.UploadFileResourceRequest;
import backend.courseshare.entity.FileResource;
import backend.courseshare.entity.Users;
import backend.courseshare.repository.FileResourceRepository;
import backend.courseshare.security.CustomUserDetails;
import backend.courseshare.service.FileResourceService;
import backend.courseshare.storage.FileStorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileResourceController {

    private final FileResourceService fileResourceService;
    private final FileResourceRepository fileResourceRepository;
    private final FileStorageService storageService;

    public FileResourceController(FileResourceService fileResourceService,
                                  FileResourceRepository fileResourceRepository,
                                  FileStorageService storageService) {
        this.fileResourceService = fileResourceService;
        this.fileResourceRepository = fileResourceRepository;
        this.storageService = storageService;
    }
    //Upload
    @PostMapping("/upload")
    public ResponseEntity<FileResourceResponse> upload(
            @RequestParam("file")MultipartFile file,
            @ModelAttribute UploadFileResourceRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) throws IOException {

        Users uploder = userDetails.getUser();

        FileResourceResponse response =fileResourceService.upload(file,request,uploder);

        return ResponseEntity.ok(response);
    }

    //List in course
    @GetMapping("/course/{courseCode}")
    public ResponseEntity<FileResourceListResponse> listFiles(
            @PathVariable String courseCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(fileResourceService.getFilesByCourse(courseCode,page,size));
    }

    //Download
    @GetMapping("/{publicId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String publicId) throws IOException {

        FileResource resource = fileResourceRepository.findByPublicId(publicId).orElseThrow(() -> new IllegalArgumentException("File Not Found"));

        Path filePath = Path.of("uploads").resolve(resource.getCourse().getCourseCode()).resolve(resource.getFilename()).toAbsolutePath();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(Files.size(filePath))
                .contentType(MediaType.parseMediaType(resource.getFileType()))
                .body(new InputStreamResource(Files.newInputStream(filePath)));
    }

    //delete
    @DeleteMapping("/{publicId}")
    public ResponseEntity<?> delete(
            @PathVariable String publicId, @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        FileResource resource = fileResourceRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("This File is Not Found"));

        Users caller = userDetails.getUser();
        boolean isOwner = resource.getUploadedBy().getId().equals(caller.getId());
        boolean isAdmin = caller.getRole().equals("ADMIN");

        if(!isOwner && !isAdmin) {
            return ResponseEntity.status(403).body("You are Not Allowed To Delete This File");
        }

        //remove from Storege
        Path filePath = Path.of("uploads").resolve(resource.getCourse().getCourseCode()).resolve(resource.getFilename());
        Files.deleteIfExists(filePath);

        return ResponseEntity.ok("Deleted Succefully");

    }

    //Search
    @GetMapping("/search")
    public  ResponseEntity<FileResourceListResponse> searchFiles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(fileResourceService.searchFiles(query,page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<FileResourceListResponse> myUploads(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (principal == null) return ResponseEntity.status(401).build();

        var response = fileResourceService.listMyUploads(principal.getPublicId(), page, size);
        return ResponseEntity.ok(response);
    }


}
