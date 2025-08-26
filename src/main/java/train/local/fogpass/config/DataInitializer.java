package train.local.fogpass.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import train.local.fogpass.entity.Role;
import train.local.fogpass.entity.User;
import train.local.fogpass.entity.UserAccessScope;
import train.local.fogpass.repository.RoleRepository;
import train.local.fogpass.repository.UserRepository;
import train.local.fogpass.security.RoleConstants;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    ApplicationRunner seedDefaults(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Ensure base roles exist
            ensureRole(roleRepository, RoleConstants.SUPER_ADMIN);
            ensureRole(roleRepository, RoleConstants.ADMIN);
            ensureRole(roleRepository, RoleConstants.LOCO_PILOT);

            final String username = "superadmin";
            final String plainPassword = "superadmin@123";
            final long suUserId = 1L; // must be unique (users.user_id is NOT NULL & unique)

            Role superAdminRole = roleRepository.findByName(RoleConstants.SUPER_ADMIN)
                    .orElseThrow();

            User existing = userRepository.findByUsername(username).orElse(null);
            if (existing == null) {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(plainPassword));
                user.setUserId(suUserId);
                user.setActive(true);

                UserAccessScope scope = new UserAccessScope();
                scope.setUser(user);
                scope.setRole(superAdminRole);
                // zone/division/section left null ⇒ global scope is implied for SUPER_ADMIN

                Set<UserAccessScope> scopes = new HashSet<>();
                scopes.add(scope);
                user.setAccessScopes(scopes);

                userRepository.save(user);
                System.out.println("[DataInitializer] Seeded SUPER_ADMIN user: " + username);
            } else {
                // Ensure SUPER_ADMIN scope exists
                boolean hasSuper = existing.getAccessScopes() != null && existing.getAccessScopes().stream()
                        .anyMatch(s -> s.getRole() != null && RoleConstants.SUPER_ADMIN.equals(s.getRole().getName()));
                if (!hasSuper) {
                    UserAccessScope scope = new UserAccessScope();
                    scope.setUser(existing);
                    scope.setRole(superAdminRole);
                    if (existing.getAccessScopes() == null) existing.setAccessScopes(new HashSet<>());
                    existing.getAccessScopes().add(scope);
                    userRepository.save(existing);
                    System.out.println("[DataInitializer] Added SUPER_ADMIN scope to user: " + username);
                }
                // If password is not BCrypt, set it to encoded known password
                String currentPwd = existing.getPassword();
                if (currentPwd == null || !(currentPwd.startsWith("$2a$") || currentPwd.startsWith("$2b$"))) {
                    existing.setPassword(passwordEncoder.encode(plainPassword));
                    userRepository.save(existing);
                    System.out.println("[DataInitializer] Updated superadmin password to encoded value.");
                }
            }
        };
    }

    private void ensureRole(RoleRepository roleRepository, String name) {
        roleRepository.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            return roleRepository.save(r);
        });
    }
}