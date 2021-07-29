package app.packed.bean.hooks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.base.Nullable;
import zandbox.internal.hooks2.bootstrap.AccessibleMethodBootstrapModel;
import zandbox.internal.hooks2.bootstrap.AccessibleMethodBootstrapModel.BootstrapContext;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor;

/**
 * A hook triggered by an annotation that allows to invoke a single method.
 */
/**
 * A bootstrap class that determines how the hook is processed.
 * <p>
 * If a field is annotated is such a way that there are multiple hooks activated at the same and athere are multiple
 * hooks that each have A single bootstrap Hvad goer vi med abstract klasser her??? Det er maaske ikke kun performance
 * at vi skal cache dem. Ellers kan vi ligesom ikke holder kontrakten om kun at aktivere det en gang...
 */
public abstract class BeanMethodHook extends AbstractBeanHook {

    /**
     * A bootstrap object using by this class. Should only be read via {@link #context()}. Updated via
     * {@link AccessibleMethodBootstrapModel}.
     */
    private @Nullable BootstrapContext context;

    // Taenker vi har lov til at smide reflection exception???
    protected void bootstrap() {}

    /** {@return the bootstrap object} */
    private BootstrapContext context() {
        // Maybe do like Assembly with a doBootstrap method
        BootstrapContext b = context;
        if (b == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return b;
    }

    /** Disables the hook, no further processing will be done. */
    public final void disable() {
        context().disable();
    }

    /**
     * Returns the modifiers of the method.
     * 
     * @return the modifiers of the method
     * @see Method#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
     */
    public final int getModifiers() {
        throw new UnsupportedOperationException();
    }

    /** {@return the method we are bootstrapping.} */
    public final Method method() {
        return processor().expose(getClass().getModule());
    }

    /**
     * Returns a method handle that gives access to the method as specified by {@link Lookup#unreflect(Method)}.
     * 
     * @return a method handle
     */
    public final MethodHandle methodHandle() {
        return processor().unreflect();
    }

    @Override
    ClassBootstrapProcessor.MethodProcessor processor() {
        return context().processor;
    }
}

class ZAddMe {

    void $metaHook(Class<? extends BeanMethodHook> hook) {
        // Bootstrap models are not cached
    }
    
    void $cacheDisable() {
        // Bootstrap models are not cached
    }
    
    // One model for ApplicationBean, other model other drivers
    void $cachePerBeanDriver() {
        // Bootstrap models are not cached
    }
}
