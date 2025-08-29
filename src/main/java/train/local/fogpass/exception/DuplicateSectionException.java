package train.local.fogpass.exception;

/**
 * Custom exception thrown when a duplicate section is created
 */
public class DuplicateSectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateSectionException(String message) {
        super(message);
    }

    public DuplicateSectionException(Long divisionId, String sectionName) {
        this("Section with name '" + sectionName + "' already exists in division id: " + divisionId);
    }
}
