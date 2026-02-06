package backend.courseshare.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class FileStorageService {
    private final Path storagePath;

    public FileStorageService(@Value("${file.storage.location}") String  storageDir) throws IOException {
        this.storagePath = Paths.get(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(this.storagePath);

    }

    public String saveFile(MultipartFile file, String courseCode) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Invalid File Name");
        }

        // Create directory for the course
        Path courseDir = storagePath.resolve(courseCode);
        Files.createDirectories(courseDir);

        // Correct: save using ORIGINAL file name
        Path targetFile = courseDir.resolve(originalName);

        // Save file to disk
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // Return relative URL for frontend/DB
        return "/uploads/" + courseCode + "/" + originalName;
    }

}
