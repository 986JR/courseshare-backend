package backend.courseshare.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SupabaseStorageService {

    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String bucket;
    private final WebClient webClient;

    public SupabaseStorageService(
            @Value("${SUPABASE_URL}") String supabaseUrl,
            @Value("${SUPABASE_KEY}") String serviceRoleKey,
            @Value("${SUPABASE_BUCKET}") String bucket
    ) {
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.bucket = bucket;

        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .build();
    }

    // ================= UPLOAD =================
    public String saveFile(MultipartFile file, String courseCode) throws IOException {

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // sanitize spaces (important)
        filename = filename.replace(" ", "-");
        courseCode = courseCode.replace(" ", "-");

        String path = courseCode + "/" + filename;

        webClient.post()
                .uri("/storage/v1/object/" + bucket + "/" + path)
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("Content-Type", file.getContentType())   // ⭐ CRITICAL FIX
                .bodyValue(file.getBytes())
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Supabase error: " + body))
                )
                .toBodilessEntity()
                .block();

        return path;
    }


    // ================= SIGNED URL =================
    public String createSignedUrl(String filePath) {

        return webClient.post()
                .uri("/storage/v1/object/sign/" + bucket + "/" + filePath + "?expiresIn=3600")
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .retrieve()
                .bodyToMono(SignedUrlResponse.class)
                .map(res -> supabaseUrl + "/storage/v1" + res.signedURL())
                .block();
    }

    // ================= DELETE =================
    public void deleteFile(String filePath) {

        webClient.delete()
                .uri("/storage/v1/object/" + bucket + "/" + filePath)
                .header("apikey", serviceRoleKey)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    // ================= DTO =================
    private record SignedUrlResponse(String signedURL) {}
}
