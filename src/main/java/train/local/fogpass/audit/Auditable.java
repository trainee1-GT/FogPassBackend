package train.local.fogpass.audit;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    AuditAction action();
    String entityType() default ""; // Optional override
    String idParam() default "id"; // Name of method parameter holding ID (if applicable)
}