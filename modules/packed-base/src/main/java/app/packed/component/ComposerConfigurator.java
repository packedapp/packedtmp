package app.packed.component;

/**
 *
 */
@FunctionalInterface
public interface ComposerConfigurator<T extends Composer<?>> {

    /**
     * Configures the given composer.
     *
     * @param t
     *            the composer that is to be configured
     */
    void configure(T t);
}
