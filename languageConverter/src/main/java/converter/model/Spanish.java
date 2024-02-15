package converter.model;

/**
 * Simple class to test converter.
 *
 * @author ndhougoda-hamal
 * @since 2.0
 */
public final class Spanish implements Language {

    /**
     * Spanish prefix.
     */
    static final String PREFIX = "Spanish-";

    private final String content;

    /**
     * Constructor.
     */
    public Spanish(String content) {
        this.content = PREFIX + content;
    }

    @Override
    public String toString() {
        return content;
    }
}
