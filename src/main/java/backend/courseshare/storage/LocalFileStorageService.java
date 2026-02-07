package backend.courseshare.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Profile("dev")
public class LocalFileStorageService implements FileStorageService1 {

    private final Path storagePath;

    public LocalFileStorageService(
            @Value("${file.storage.location}") String storageDir
    ) throws IOException {
        this.storagePath = Paths.get(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(this.storagePath);
    }

    @Override
    public String saveFile(MultipartFile file, String courseCode) throws IOException {

        String originalName = file.getOriginalFilename();

        Path courseDir = storagePath.resolve(courseCode);
        Files.createDirectories(courseDir);

        Path targetFile = courseDir.resolve(originalName);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + courseCode + "/" + originalName;
    }
}

