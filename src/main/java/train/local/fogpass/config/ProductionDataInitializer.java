package train.local.fogpass.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import train.local.fogpass.entity.Role;
import train.local.fogpass.entity.User;
import train.local.fogpass.entity.UserAccessScope;
import train.local.fogpass.repository.RoleRepository;
import train.local.fogpass.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Production-safe data initializer that only creates essential system data
 * Runs only in production profile and creates secure default admin
 */
@Slf4j
@Component
@Profile("prod") // Only run in production profile
@RequiredArgsConstructor
public class ProductionDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Production data initialization started...");
        
        initializeEssentialRoles();
        initializeProductionSuperAdmin();
        
        log.info("✅ Production data initialization completed successfully.");
    }

    /**
     * Initialize only essential roles for production
     */
    private void initializeEssentialRoles() {
        log.info("Initializing essential system roles...");
        
        createRoleIfNotExists("SUPER_ADMIN");
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("LOCO_PILOT");
        
        log.info("✅ Essential roles initialized.");
    }

    /**
     * Initialize super admin with secure random password for production
     */
    private void initializeProductionSuperAdmin() {
        log.info("Checking for SUPER_ADMIN user...");
        
        // Check if super admin already exists
        Optional<User> existingSuperAdmin = userRepository.findByUsername("superadmin");
        if (existingSuperAdmin.isPresent()) {
            log.info("✅ SUPER_ADMIN user already exists in production.");
            return;
        }

        // Get environment-specific password or generate secure one
        String adminPassword = getSecureAdminPassword();
        
        // Get SUPER_ADMIN role
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

        // Create production super admin user
        User superAdmin = new User();
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword(passwordEncoder.encode(adminPassword));
        superAdmin.setUserId(99999L);
        superAdmin.setDesignation("System Administrator");
        superAdmin.setDepartment("IT Operations");
        superAdmin.setActive(true);

        // Create global access scope
        UserAccessScope globalScope = new UserAccessScope();
        globalScope.setUser(superAdmin);
        globalScope.setRole(superAdminRole);
        globalScope.setZoneId(null);
        globalScope.setDivisionId(null);
        globalScope.setSectionId(null);

        superAdmin.getAccessScopes().add(globalScope);

        // Save user
        User savedUser = userRepository.save(superAdmin);
        
        log.info("🔐 SUPER_ADMIN user created for production!");
        log.info("   Username: 'superadmin'");
        log.info("   User ID: {}", savedUser.getId());
        
        // Only log password info if it was generated (not from environment)
        if (!isPasswordFromEnvironment()) {
            log.warn("⚠️  CRITICAL: Generated password for superadmin: {}", adminPassword);
            log.warn("⚠️  SAVE THIS PASSWORD IMMEDIATELY - IT WILL NOT BE SHOWN AGAIN!");
            log.warn("⚠️  Change this password after first login!");
        } else {
            log.info("✅ Using password from environment configuration.");
        }
    }

    /**
     * Get secure admin password from environment or generate one
     */
    private String getSecureAdminPassword() {
        // Try to get password from environment variable
        String envPassword = environment.getProperty("FOGPASS_ADMIN_PASSWORD");
        if (envPassword != null && !envPassword.trim().isEmpty()) {
            log.info("Using admin password from environment variable.");
            return envPassword.trim();
        }

        // Try to get from application properties
        String propPassword = environment.getProperty("fogpass.admin.password");
        if (propPassword != null && !propPassword.trim().isEmpty()) {
            log.info("Using admin password from application properties.");
            return propPassword.trim();
        }

        // Generate secure random password
        log.warn("No admin password found in environment. Generating secure random password...");
        return generateSecurePassword();
    }

    /**
     * Check if password is from environment configuration
     */
    private boolean isPasswordFromEnvironment() {
        String envPassword = environment.getProperty("FOGPASS_ADMIN_PASSWORD");
        String propPassword = environment.getProperty("fogpass.admin.password");
        return (envPassword != null && !envPassword.trim().isEmpty()) || 
               (propPassword != null && !propPassword.trim().isEmpty());
    }

    /**
     * Generate cryptographically secure random password
     */
    private String generateSecurePassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = upperCase + lowerCase + digits + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each category
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Fill remaining positions with random characters
        for (int i = 4; i < 16; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * Create role if it doesn't exist
     */
    private Role createRoleIfNotExists(String name) {
        Optional<Role> existingRole = roleRepository.findByName(name);
        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        Role newRole = new Role();
        newRole.setName(name);
        return roleRepository.save(newRole);
    }
}