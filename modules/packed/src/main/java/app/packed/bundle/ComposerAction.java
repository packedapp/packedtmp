package app.packed.bundle;

/**
 *
 */
@FunctionalInterface
public interface ComposerAction<C extends Composer> {

    /**
     * Configures the given composer.
     *
     * @param composer
     *            the composer that is to be configured
     */
    void build(C composer);
}
