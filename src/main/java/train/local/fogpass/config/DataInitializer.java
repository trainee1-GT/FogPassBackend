package train.local.fogpass.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import train.local.fogpass.entity.Division;
import train.local.fogpass.entity.Role;
import train.local.fogpass.entity.User;
import train.local.fogpass.entity.UserAccessScope;
import train.local.fogpass.entity.Zone;
import train.local.fogpass.repository.DivisionRepository;
import train.local.fogpass.repository.RoleRepository;
import train.local.fogpass.repository.UserRepository;
import train.local.fogpass.repository.ZoneRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Profile({"dev", "test"}) // Only run in dev and test profiles
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // Inject all necessary repositories and the password encoder
    private final ZoneRepository zoneRepository;
    private final DivisionRepository divisionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize in proper order: Roles -> Users -> Zones -> Divisions
        log.info("Starting data initialization...");
        
        initializeRoles();
        initializeSuperAdmin();
        
        if (zoneRepository.count() == 0) {
            log.info("Initializing master data...");
            initializeZones();
            initializeDivisions();
            log.info("Master data initialization completed.");
        } else {
            log.info("Master data already exists. Skipping zone/division initialization.");
        }
        
        log.info("Data initialization completed successfully.");
    }

    /**
     * Initialize system roles
     */
    private void initializeRoles() {
        log.info("Initializing system roles...");
        
        createRoleIfNotExists("SUPER_ADMIN", "Super Administrator with full system access");
        createRoleIfNotExists("ADMIN", "Administrator with limited scope access");
        createRoleIfNotExists("LOCO_PILOT", "Locomotive Pilot with operational access");
        
        log.info("System roles initialization completed.");
    }

    /**
     * Initialize super admin user
     */
    private void initializeSuperAdmin() {
        log.info("Initializing SUPER_ADMIN user...");
        
        // Check if super admin already exists
        Optional<User> existingSuperAdmin = userRepository.findByUsername("superadmin");
        if (existingSuperAdmin.isPresent()) {
            log.info("SUPER_ADMIN user already exists. Skipping creation.");
            return;
        }

        // Get SUPER_ADMIN role
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

        // Create super admin user
        User superAdmin = new User();
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword(passwordEncoder.encode("admin123")); // CRITICAL: Always encode!
        superAdmin.setUserId(99999L); // Unique system user ID
        superAdmin.setDesignation("System Administrator");
        superAdmin.setDepartment("IT Operations");
        superAdmin.setActive(true);
        superAdmin.setMobNo("9999999999"); // System contact

        // Create UserAccessScope for SUPER_ADMIN (global access)
        UserAccessScope globalScope = new UserAccessScope();
        globalScope.setUser(superAdmin);
        globalScope.setRole(superAdminRole);
        // For SUPER_ADMIN: zoneId, divisionId, sectionId are NULL for universal access
        globalScope.setZoneId(null);
        globalScope.setDivisionId(null);
        globalScope.setSectionId(null);

        // Add scope to user
        superAdmin.getAccessScopes().add(globalScope);

        // Save user (cascade will save the scope)
        User savedUser = userRepository.save(superAdmin);
        
        log.info("✅ SUPER_ADMIN user created successfully!");
        log.info("   Username: 'superadmin'");
        log.info("   Password: 'admin123'");
        log.info("   User ID: {}", savedUser.getId());
        log.info("   ⚠️  IMPORTANT: Change default password in production!");
    }

    /**
     * Create role if it doesn't exist
     */
    private Role createRoleIfNotExists(String name, String description) {
        Optional<Role> existingRole = roleRepository.findByName(name);
        if (existingRole.isPresent()) {
            log.debug("Role '{}' already exists", name);
            return existingRole.get();
        }

        Role newRole = new Role();
        newRole.setName(name);
        Role savedRole = roleRepository.save(newRole);
        
        log.info("✅ Created role: '{}'", name);
        return savedRole;
    }

    private void initializeZones() {
        List<Zone> zones = Arrays.asList(
            createZone("Western Railway Zone", "WR", 
                "Western Railway Zone covering Mumbai, Pune, Ahmedabad and surrounding areas including suburban networks"),
            createZone("Central Railway Zone", "CR", 
                "Central Railway Zone covering central India including Mumbai Central, Nagpur, and major freight corridors"),
            createZone("Northern Railway Zone", "NR", 
                "Northern Railway Zone covering Delhi, Punjab, Haryana, Himachal Pradesh and Jammu & Kashmir regions"),
            createZone("Southern Railway Zone", "SR", 
                "Southern Railway Zone covering Tamil Nadu, Kerala, Karnataka and parts of Andhra Pradesh"),
            createZone("Eastern Railway Zone", "ER", 
                "Eastern Railway Zone covering West Bengal, Jharkhand, Bihar and parts of Odisha"),
            createZone("South Eastern Railway Zone", "SER", 
                "South Eastern Railway Zone covering parts of West Bengal, Odisha, Jharkhand and Chhattisgarh"),
            createZone("North Eastern Railway Zone", "NER", 
                "North Eastern Railway Zone covering Uttar Pradesh, Bihar and parts of Madhya Pradesh"),
            createZone("South Central Railway Zone", "SCR", 
                "South Central Railway Zone covering Telangana, Andhra Pradesh and parts of Maharashtra")
        );

        zoneRepository.saveAll(zones);
        log.info("Initialized {} zones", zones.size());
    }

    private void initializeDivisions() {
        // Get saved zones
        Zone wr = zoneRepository.findByCode("WR").orElse(null);
        Zone cr = zoneRepository.findByCode("CR").orElse(null);
        Zone nr = zoneRepository.findByCode("NR").orElse(null);
        Zone sr = zoneRepository.findByCode("SR").orElse(null);

        if (wr == null || cr == null || nr == null || sr == null) {
            log.error("Zones not found. Cannot initialize divisions.");
            return;
        }

        List<Division> divisions = Arrays.asList(
            // Western Railway Divisions
            createDivision("Mumbai Division", "MUM", 
                "Mumbai Division covering Mumbai suburban network, local trains, and Western line operations", wr),
            createDivision("Vadodara Division", "VAD", 
                "Vadodara Division covering Gujarat region including Ahmedabad, Surat, and industrial corridors", wr),
            createDivision("Rajkot Division", "RJT", 
                "Rajkot Division covering Saurashtra region of Gujarat including coastal and inland routes", wr),
            createDivision("Ratlam Division", "RTM", 
                "Ratlam Division covering parts of Madhya Pradesh and Rajasthan with major junction operations", wr),

            // Central Railway Divisions
            createDivision("Mumbai Central Division", "CSTM", 
                "Mumbai Central Division covering Central line suburban network and long-distance operations", cr),
            createDivision("Pune Division", "PUNE", 
                "Pune Division covering Pune metropolitan area, Deccan region, and hill station routes", cr),
            createDivision("Solapur Division", "SUR", 
                "Solapur Division covering southern Maharashtra and northern Karnataka border regions", cr),
            createDivision("Nagpur Division", "NGP", 
                "Nagpur Division covering central India hub with major freight and passenger operations", cr),

            // Northern Railway Divisions
            createDivision("Delhi Division", "DLI", 
                "Delhi Division covering National Capital Region with major terminal and suburban operations", nr),
            createDivision("Ambala Division", "UMB", 
                "Ambala Division covering Punjab, Haryana, and Himachal Pradesh with hill railway connections", nr),
            createDivision("Firozpur Division", "FZR", 
                "Firozpur Division covering Punjab border areas with Pakistan and agricultural regions", nr),
            createDivision("Lucknow Division", "LKO", 
                "Lucknow Division covering Uttar Pradesh capital region with major passenger and freight traffic", nr),

            // Southern Railway Divisions
            createDivision("Chennai Division", "MAS", 
                "Chennai Division covering Tamil Nadu capital region with major port and suburban operations", sr),
            createDivision("Madurai Division", "MDU", 
                "Madurai Division covering southern Tamil Nadu with temple city and agricultural regions", sr),
            createDivision("Trichy Division", "TPJ", 
                "Trichy Division covering central Tamil Nadu with major junction and industrial areas", sr),
            createDivision("Salem Division", "SA", 
                "Salem Division covering western Tamil Nadu with steel city and textile regions", sr)
        );

        divisionRepository.saveAll(divisions);
        log.info("Initialized {} divisions", divisions.size());
    }

    private Zone createZone(String name, String code, String description) {
        Zone zone = new Zone();
        zone.setName(name);
        zone.setCode(code);
        zone.setDescription(description);
        return zone;
    }

    private Division createDivision(String name, String code, String description, Zone zone) {
        Division division = new Division();
        division.setName(name);
        division.setCode(code);
        division.setDescription(description);
        division.setZone(zone);
        return division;
    }
}