package converter.model;

/**
 * Simple class to test converter.
 *
 * @author ndhougoda-hamal
 * @since 2.0
 */
public final class French implements Language {

    /**
     * Spanish prefix.
     */
    static final String PREFIX = "French-";

    private final String content;

    /**
     * Constructor.
     */
    public French(Spanish content) {
        this.content = PREFIX + content.toString().split("-")[1];
    }

    @Override
    public String toString() {
        return content;
    }
}
