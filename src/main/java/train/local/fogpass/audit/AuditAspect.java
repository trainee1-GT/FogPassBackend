package train.local.fogpass.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import train.local.fogpass.entity.AuditLog;
import train.local.fogpass.repository.AuditLogRepository;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        String actor = resolveActor();
        String action = auditable.action().name();
        String args = Arrays.toString(pjp.getArgs());

        try {
            Object result = pjp.proceed();
            String details = "SUCCESS args=" + args;
            auditLogRepository.save(new AuditLog(action, actor, details));
            return result;
        } catch (Throwable ex) {
            String details = "FAILURE args=" + args + ", error=" + ex.getMessage();
            auditLogRepository.save(new AuditLog(action, actor, details));
            throw ex;
        }
    }

    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "system";
        String name = auth.getName();
        if (name == null || name.equalsIgnoreCase("anonymousUser")) return "system";
        return name;
        }
}