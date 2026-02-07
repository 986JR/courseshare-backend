package backend.courseshare.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService1{
    String saveFile(MultipartFile file, String courseCode) throws IOException;
}

