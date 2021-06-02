package zandbox.internal.hooks2.bootstrap;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import packed.internal.invoke.Infuser;
import packed.internal.util.ThrowableUtil;

public abstract class AbstractBootstrapModel {

    /** The extension this bootstrap belongs to. */
    // We need to store this info here...
    // And
    protected final Class<? extends Extension> extensionClass;

    protected final MethodHandle mhConstructor;

    protected final Class<?> bootstrapClass;

    AbstractBootstrapModel(Class<?> bootstrapClass, Class<? extends Extension> extensionClass) {
        this.bootstrapClass = requireNonNull(bootstrapClass);
        this.extensionClass = requireNonNull(extensionClass);
        Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), bootstrapClass);
        this.mhConstructor = builder.findConstructor(bootstrapClass, e -> new InternalExtensionException(e));
    }

    @SuppressWarnings("unchecked")
    protected <T> T newInstance() {
        Object instance;
        try {
            instance = mhConstructor.invoke();
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return (T) instance;
    }

    /**
     * @param processor
     *            the processor
     * 
     * @apiNote an alternative implementation strategy would be to have a special interface that
     *          {@link AccessibleFieldBootstrapModel} and {@link InjectableVariableBootstrapModel} both implemented. We
     *          initially used that model. But having this method avoids having to add an extra interface.
     */
    public void bootstrapField(ClassBootstrapProcessor.FieldProcessor processor) {
        throw new UnsupportedOperationException();
    }

    public void bootstrapMethod(ClassBootstrapProcessor.MethodProcessor processor) {
        throw new UnsupportedOperationException();
    }

    public final Class<? extends Extension> extensionClass() {
        return extensionClass;
    }
}