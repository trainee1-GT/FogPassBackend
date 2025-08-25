package train.local.fogpass.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import train.local.fogpass.entity.User;
import train.local.fogpass.entity.UserAccessScope;

import java.util.*;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    // Immutable detached view of a user's scope
    public static class ScopeView {
        private final Long zoneId;
        private final Long divisionId;
        private final Long sectionId;
        private final String roleName;

        public ScopeView(Long zoneId, Long divisionId, Long sectionId, String roleName) {
            this.zoneId = zoneId;
            this.divisionId = divisionId;
            this.sectionId = sectionId;
            this.roleName = roleName;
        }

        public Long getZoneId() { return zoneId; }
        public Long getDivisionId() { return divisionId; }
        public Long getSectionId() { return sectionId; }
        public String getRoleName() { return roleName; }
    }

    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final Set<ScopeView> accessScopes; // detached scopes

    private UserPrincipal(Long id, String username, String password, String fullName, Set<ScopeView> accessScopes) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.accessScopes = accessScopes != null ? Collections.unmodifiableSet(accessScopes) : Set.of();
    }

    public static UserPrincipal fromUser(User user) {
        Set<ScopeView> scopes = user.getAccessScopes() == null ? Set.of() : user.getAccessScopes().stream()
                .map(s -> new ScopeView(
                        s.getZoneId(),
                        s.getDivisionId(),
                        s.getSectionId(),
                        s.getRole() != null ? s.getRole().getName() : null
                ))
                .collect(Collectors.toSet());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getUsername(), // change to user.getFullName() if available
                scopes
        );
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public Set<ScopeView> getAccessScopes() { return accessScopes; }

    public List<String> getRoleNames() {
        return accessScopes.stream()
                .map(ScopeView::getRoleName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public List<Map<String, Object>> getScopesAsMaps() {
        return accessScopes.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("zoneId", s.getZoneId());
            m.put("divisionId", s.getDivisionId());
            m.put("sectionId", s.getSectionId());
            m.put("roleName", s.getRoleName());
            return m;
        }).toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoleNames().stream()
                .map(r -> "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
