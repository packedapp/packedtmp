package app.packed.bean;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.component.ComponentConfiguration;
import app.packed.state.sandbox.InstanceState;

/**
 * 
 * {@code BeanConfiguration} is the superclass of the various bean configuration classes available in Packed.
 * 
 */
@SuppressWarnings("rawtypes")
public abstract sealed class BeanConfiguration<T>
        extends ComponentConfiguration permits ApplicationBeanConfiguration,ManagedBeanConfiguration,UnmanagedBeanConfiguration {

    public <E> BeanConfiguration<T> inject(Class<E> key, E instance) {
        return inject(Key.of(key), instance);
    }

    public <E> BeanConfiguration<T> decorate(Key<E> key, Function<E, E> mapper) {
        /// Mnahhh
        throw new UnsupportedOperationException();
    }

    // Taenker den overrider
    public <E> BeanConfiguration<T> inject(Key<E> key, E instance) {
        throw new UnsupportedOperationException();
    }

    // Ved ikke praecis hvad den overskriver...

    // Kunne ogsaa vaere bind
    @SuppressWarnings("unchecked")
    public BeanConfiguration<T> inject(Object instance) {
        return inject((Class) instance.getClass(), instance);
    }

    /** {@return the kind of bean that is being configured. } */
    public abstract BeanKind kind();

    /**
     * This method can be overridden to return a subclass of bean mirror.
     * 
     * {@inheritDoc}
     * 
     * @throws IllegalStateException
     *             if the configuration has not been wired yet
     */
    @Override
    protected BeanMirror mirror() {
        // Jeg taenker det er er
        throw new UnsupportedOperationException();
    }

    /**
     * @param state
     *            the state at which to perform the specified action
     * @param action
     *            the action to perform
     * @return this configuration
     * @throws IllegalArgumentException
     *             if terminated and bean does not support it
     */
    // Ved ikke om det kan vaere problematisk, hvis instanserne ikke er styret af packed
    // Det der er farligt her er at vi capture Assemblien. Som capture extensionen
    // Som capture alt andet
    public BeanConfiguration<T> on(InstanceState state, Consumer<T> action) {
        // Maybe throw UOE instead of IAE
        return this;
    }
}
