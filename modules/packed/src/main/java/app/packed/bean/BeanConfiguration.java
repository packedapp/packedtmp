package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.component.ComponentConfiguration;
import app.packed.extension.ExtensionBeanConfiguration;
import app.packed.lifecycle.RunState;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.PackedBeanHandle;

/**
 * 
 * {@code BeanConfiguration} is the superclass of the various bean configuration classes available in Packed.
 * 
 */
@SuppressWarnings("rawtypes")
public abstract sealed class BeanConfiguration<T> extends
        ComponentConfiguration permits ContainerBeanConfiguration,ManagedBeanConfiguration,UnmanagedBeanConfiguration,FunctionalBeanConfiguration,ExtensionBeanConfiguration {

    /** The component we are configuring. Is initially null until initialized by someone. */
    private final BeanSetup bean;
    
    protected BeanConfiguration(BeanHandle<T> handle) {
        PackedBeanHandle<?> bh = requireNonNull((PackedBeanHandle<?>) handle, "handle is null");
        this.bean = bh.newSetup();
    }

    @Override
    protected void checkIsWiring() {
        bean.checkIsWiring();
    }

    // Hmm, vi dekorere ikke fx ServiceLocator...
    // Maaske er det bedre at dekorere typer???
    //// InjectableVarSelector<T>
    // InjectableVarSelector.keyedOf()
    public <E> BeanConfiguration<T> decorate(Key<E> key, Function<E, E> mapper) {
        /// Mnahhh
        throw new UnsupportedOperationException();
    }

    // Hmm det er jo mere provide end inject..
    // men provide(FooClass.class).provide(ddd.Class);
    // maybe provideTo()
    public <E> BeanConfiguration<T> inject(Class<E> key, E instance) {
        return inject(Key.of(key), instance);
    }

    
    // Taenker den overrider
    public <E> BeanConfiguration<T> inject(Key<E> key, E instance) {
        throw new UnsupportedOperationException();
    }

    // Kunne ogsaa vaere bind
    @SuppressWarnings("unchecked")
    public BeanConfiguration<T> inject(Object instance) {
        return inject((Class) instance.getClass(), instance);
    }

    /** {@return the kind of bean that is being configured. } */
    public abstract BeanKind kind();

    // Ved ikke praecis hvad den overskriver...

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

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration<T> named(String name) {
        bean.named(name);
        return this;
    }

    /**
     * @param state
     *            the state at which to perform the specified action
     * @param action
     *            the action to perform
     * @return this configuration
     * @throws IllegalArgumentException
     *             if terminated and bean does not support it
     * @throws UnsupportedOperationException
     *             if the bean is stateless
     */
    // Ved ikke om det kan vaere problematisk, hvis instanserne ikke er styret af packed
    // Det der er farligt her er at vi capture Assemblien. Som capture extensionen
    // Som capture alt andet
    ///// fx Validator beans vil ikke virke her...
    public BeanConfiguration<T> on(RunState state, Consumer<T> action) {
        // Maybe throw UOE instead of IAE
        return this;
    }

    @Override
    public NamespacePath path() {
        return bean.path();
    }

    @Override
    public String toString() {
        return bean.toString();
    }
}
