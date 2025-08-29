package train.local.fogpass.exception;

/**
 * Custom exception thrown when a Section entity is not found
 */
public class SectionNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SectionNotFoundException(String message) {
        super(message);
    }

    public SectionNotFoundException(Long id) {
        this("Section not found with id: " + id);
    }
}
