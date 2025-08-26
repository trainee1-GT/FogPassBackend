package train.local.fogpass.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncryptionTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void testPasswordEncoding() {
        String plainPassword = "admin123";
        
        // Encode password
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        // Verify encoding
        assertNotNull(encodedPassword);
        assertNotEquals(plainPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
        assertTrue(encodedPassword.length() > 50);
        
        System.out.println("Plain Password: " + plainPassword);
        System.out.println("Encoded Password: " + encodedPassword);
    }

    @Test
    public void testPasswordMatching() {
        String plainPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        // Test correct password
        assertTrue(passwordEncoder.matches(plainPassword, encodedPassword));
        
        // Test incorrect password
        assertFalse(passwordEncoder.matches("wrongpassword", encodedPassword));
    }

    @Test
    public void testSaltUniqueness() {
        String plainPassword = "admin123";
        
        // Encode same password multiple times
        String hash1 = passwordEncoder.encode(plainPassword);
        String hash2 = passwordEncoder.encode(plainPassword);
        String hash3 = passwordEncoder.encode(plainPassword);
        
        // Each hash should be different due to unique salts
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash1, hash3);
        
        // But all should match the original password
        assertTrue(passwordEncoder.matches(plainPassword, hash1));
        assertTrue(passwordEncoder.matches(plainPassword, hash2));
        assertTrue(passwordEncoder.matches(plainPassword, hash3));
        
        System.out.println("Same password, different hashes:");
        System.out.println("Hash 1: " + hash1);
        System.out.println("Hash 2: " + hash2);
        System.out.println("Hash 3: " + hash3);
    }

    @Test
    public void testBCryptFormat() {
        String plainPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        // BCrypt format: $2a$rounds$salt+hash
        String[] parts = encodedPassword.split("\\$");
        
        assertEquals(4, parts.length); // ["", "2a", "10", "salt+hash"]
        assertEquals("2a", parts[1]); // BCrypt version
        assertEquals("10", parts[2]); // Default rounds
        assertEquals(53, parts[3].length()); // Salt (22) + Hash (31)
        
        System.out.println("BCrypt Format Analysis:");
        System.out.println("Full Hash: " + encodedPassword);
        System.out.println("Version: $" + parts[1] + "$");
        System.out.println("Rounds: " + parts[2]);
        System.out.println("Salt+Hash Length: " + parts[3].length());
    }

    @Test
    public void testPerformance() {
        String plainPassword = "performanceTest123";
        
        long startTime = System.currentTimeMillis();
        
        // Encode 10 passwords to test performance
        for (int i = 0; i < 10; i++) {
            passwordEncoder.encode(plainPassword + i);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Time to encode 10 passwords: " + duration + "ms");
        System.out.println("Average time per password: " + (duration / 10.0) + "ms");
        
        // BCrypt should be slow (security feature)
        assertTrue(duration > 50, "BCrypt should take some time to compute");
    }
}