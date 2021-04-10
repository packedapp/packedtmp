package app.packed.component;

@FunctionalInterface
public interface ComposerConfigurator<T extends Composer<?>> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void configure(T t);
}
