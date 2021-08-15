package app.packed.container;

/**
 *
 */
// Kan ikke rigtig Builder...
// ComposerAction (rename to build
@FunctionalInterface
public interface ComposerAction<T extends Composer> {

    /**
     * Configures the given composer.
     *
     * @param t
     *            the composer that is to be configured
     */
    void build(T t); // build??? Kan ikke se hvorfor den ikke skal hedde det samme som assembly
}
