package com.klkt.supervision.service;

import com.klkt.supervision.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupCodeService {
    
    private final GroupRepository groupRepository;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final Random random = new Random();
    
    /**
     * Generate a memorable group code in format: ABC123 (3 letters + 3 numbers)
     * @return Mono containing the generated group code
     */
    public Mono<String> generateGroupCode() {
        return generateUniqueCode(0, 100);
    }
    
    private Mono<String> generateUniqueCode(int attempts, int maxAttempts) {
        if (attempts >= maxAttempts) {
            return Mono.error(new RuntimeException(
                    "Failed to generate unique group code after " + maxAttempts + " attempts"));
        }
        
        // Generate 3 random uppercase letters
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            letters.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        
        // Generate 3 random numbers
        StringBuilder numbers = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            numbers.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        
        String code = letters.toString() + numbers.toString();
        
        // Check if code already exists
        return groupRepository.findByGroupCode(code)
                .flatMap(existingGroup -> {
                    // Code exists, try again
                    log.debug("Group code {} already exists, generating new one...", code);
                    return generateUniqueCode(attempts + 1, maxAttempts);
                })
                .switchIfEmpty(Mono.just(code))
                .doOnNext(generatedCode -> log.info("Generated unique group code: {}", generatedCode));
    }
}

