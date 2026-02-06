package backend.courseshare.ratelimiting;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key,this::newBucket);
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                100,
                Refill.intervally(100, Duration.ofMinutes(1))
        );

        return Bucket.builder().addLimit(limit).build();
    }
}
