package train.local.fogpass.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/journeys")
@PreAuthorize("hasRole(T(train.local.fogpass.security.RoleConstants).LOCO_PILOT) or hasRole(T(train.local.fogpass.security.RoleConstants).SUPER_ADMIN)")
public class JourneyController {
    // Add endpoints as needed; RBAC already enforced via class-level pre-authorization
}
