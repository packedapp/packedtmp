package app.packed.component;

/**
 *
 */
// Kan ikke rigtig Builder...
// ComposerAction (rename to build
@FunctionalInterface
public interface ComposerConfigurator<T extends Composer<?>> {

    /**
     * Configures the given composer.
     *
     * @param t
     *            the composer that is to be configured
     */
    void configure(T t); // build??? Kan ikke se hvorfor den ikke skal hedde det samme som assembly
}
