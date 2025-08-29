package train.local.fogpass.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final ApplicationEventPublisher publisher;
    private final AuditContext auditContext;
    private final ChangeLogUtil changeLogUtil;
    private final AuditProperties properties;
    private final ObjectMapper objectMapper;

    @Around("@annotation(train.local.fogpass.audit.Auditable)")
    public Object aroundAuditable(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Auditable ann = method.getAnnotation(Auditable.class);

        Object beforeDto = null;
        Object result = null;
        Exception error = null;
        try {
            // For UPDATE/DELETE, capture first argument as before-state identifier if DTO is provided
            if (ann.action() == AuditAction.UPDATE || ann.action() == AuditAction.DELETE) {
                // Best-effort: if first arg is DTO, keep a copy; otherwise, skip
                Object[] args = pjp.getArgs();
                if (args.length > 0) {
                    beforeDto = deepCopy(args[args.length - 1]); // usually last arg is request DTO
                }
            }
            result = pjp.proceed();
        } catch (Exception ex) {
            error = ex;
            throw ex;
        } finally {
            if (error == null) {
                emitAuditEvent(ann, pjp.getArgs(), result, beforeDto);
            }
        }
        return result;
    }

    private void emitAuditEvent(Auditable ann, Object[] args, Object result, Object beforeDto) {
        try {
            String entityType = !ann.entityType().isBlank() ? ann.entityType() : resolveEntityType(result);
            String entityId = resolveEntityId(args, result, ann.idParam());

            Map<String, Object> diff = null;
            if ((ann.action() == AuditAction.UPDATE || ann.action() == AuditAction.DELETE) && beforeDto != null) {
                diff = changeLogUtil.diff(beforeDto, result, properties.getMaskedFields());
            }

            Object maskedSnapshotObj = changeLogUtil.maskTree(result, properties.getMaskedFields());
            String snapshot = changeLogUtil.toJson(maskedSnapshotObj);

            AuditEvent event = AuditEvent.builder()
                    .action(ann.action())
                    .entityType(entityType)
                    .entityId(entityId)
                    .performedBy(Optional.ofNullable(auditContext.getUsername()).orElse("system"))
                    .ipAddress(auditContext.getIpAddress())
                    .requestId(auditContext.getRequestId())
                    .userAgent(auditContext.getUserAgent())
                    .diff(diff)
                    .snapshotJson(snapshot)
                    .maskedFields(properties.getMaskedFields())
                    .build();

            publisher.publishEvent(event);
        } catch (Exception e) {
            log.warn("Non-fatal audit emission failure: {}", e.getMessage());
        }
    }

    private String resolveEntityType(Object result) {
        return result != null ? result.getClass().getSimpleName() : "Unknown";
    }

    private String resolveEntityId(Object[] args, Object result, String idParam) {
        // Try to find id in args by naming convention or pick from result if has getId
        for (Object arg : args) {
            if (arg == null) continue;
            try {
                var m = arg.getClass().getMethod("getId");
                Object id = m.invoke(arg);
                if (id != null) return String.valueOf(id);
            } catch (Exception ignore) {}
        }
        if (result != null) {
            try {
                var m = result.getClass().getMethod("getId");
                Object id = m.invoke(result);
                if (id != null) return String.valueOf(id);
            } catch (Exception ignore) {}
        }
        return null;
    }

    private Object masked(Object obj) {
        // For now, rely on ChangeLogUtil mask on field names within maps; converting will mask values
        return obj;
    }

    private Object deepCopy(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, obj.getClass());
        } catch (Exception e) {
            return obj; // fallback to same reference if copy fails
        }
    }
}