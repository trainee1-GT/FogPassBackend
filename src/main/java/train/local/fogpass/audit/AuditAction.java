package train.local.fogpass.audit;

/**
 * Enum representing different types of audit actions
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    READ,
    ROLE_ASSIGN,
    LOGIN,
    LOGOUT,
    PASSWORD_RESET,
    PASSWORD_CHANGE,
    ENABLE,
    DISABLE,
    CREATE_SECTION,   // ✅ added as enum constant
    UPDATE_SECTION,
    DELETE_SECTION;
}
