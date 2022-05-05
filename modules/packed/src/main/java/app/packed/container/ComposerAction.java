package app.packed.container;

/**
 *
 */
@FunctionalInterface
public interface ComposerAction<C extends AbstractComposer> {

    /**
     * Configures the given composer.
     *
     * @param composer
     *            the composer that is to be configured
     */
    void build(C composer);
}
