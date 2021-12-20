package zandbox.internal.hooks2.bootstrap;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

import app.packed.extension.Extension;
import app.packed.hooks.BeanField;
import packed.internal.container.ExtensionModel;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TmpLoader;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor.FieldProcessor;
import zandbox.packed.hooks.AccessibleFieldHook;

/** A model create from an {@link AccessibleFieldHook} annotation. */
public final class AccessibleFieldBootstrapModel extends AbstractBootstrapModel {

    // Maaske i virkeligheden bare have et ConcurrentHashMap<Thread, AFH>?
    private static final ThreadLocal<AccessibleFieldHook> ANNOTATION = new ThreadLocal<>();

    /** A MethodHandle that can invoke {@link BeanField#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), AccessibleFieldHook.Bootstrap.class,
            "bootstrap", void.class);

    private static final ClassValue<AccessibleFieldBootstrapModel> MODELS = new ClassValue<AccessibleFieldBootstrapModel>() {

        @Override
        protected AccessibleFieldBootstrapModel computeValue(Class<?> type) {
            AccessibleFieldHook afh = ANNOTATION.get();
            ExtensionModel.of(afh.extension()); // checks that it is a valid extension..
            assert afh.bootstrapBean() == type;
            BootstrapClassLoader loader = TL.initializeClass(type);
            return new AccessibleFieldBootstrapModel(afh.bootstrapBean(), loader, afh.extension());
        }
    };

    private static final TmpLoader<BootstrapClassLoader> TL = new TmpLoader<>(BootstrapClassLoader.class, t -> new BootstrapClassLoader());

    /** A VarHandle that can access {@link BeanField#processor}. */
    private static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), AccessibleFieldHook.Bootstrap.class,
            "context", AccessibleFieldBootstrapModel.BootstrapContext.class);

    private AccessibleFieldBootstrapModel(Class<? extends AccessibleFieldHook.Bootstrap> bootstrapClass, BootstrapClassLoader loader,
            Class<? extends Extension> extensionClass) {
        super(bootstrapClass, extensionClass);
    }

    /** {@inheritDoc} */
    @Override
    public void bootstrapField(ClassBootstrapProcessor.FieldProcessor processor) {
        BootstrapContext b = new BootstrapContext(processor);

        AccessibleFieldHook.Bootstrap instance = newInstance();

        VH_FIELD_HOOK_BUILDER.set(instance, b);

        // Invoke ClassHook.Bootstrap#bootstrap()
        try {
            MH_FIELD_HOOK_BOOTSTRAP.invokeExact(instance);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        VH_FIELD_HOOK_BUILDER.set(instance, null);

        // don't do anything if disabled
        if (b.disabled) {
            return;
        }

        // System.out.println("Created a hook on " + processor.expose(getClass().getModule()));
        // invoke runner
    }

    /**
     * Creates a bootstrap model from a {@link AccessibleFieldHook} annotation.
     * 
     * @param annotation
     *            the annotation to create a model for
     * @return the new model
     */
    public static AccessibleFieldBootstrapModel of(AccessibleFieldHook annotation) {
        Class<? extends AccessibleFieldHook.Bootstrap> bootstrapClass = annotation.bootstrapBean();
        // The main issue here is that we want to store the extension
        // We use a ThreadLocal to pass the extension from boostrapClass to the class value.
        // We do this because we need to make sure that
        AccessibleFieldBootstrapModel model;
        assert ANNOTATION.get() == null; // We scan the bootstrap remember...
        ANNOTATION.set(annotation);
        try {
            model = MODELS.get(bootstrapClass);
        } finally {
            ANNOTATION.remove();
        }

        if (model.extensionClass != annotation.extension()) {
            throw new IllegalStateException("If multiple hooks share the same bootstrap class, they must all have the same extension class");
        }
        return model;
    }

    /**
     * @param callerClass
     *            the calling class (must a proper subclass of {@link AccessibleFieldHook.Bootstrap})
     * @return
     */
    public static void staticInitialize(Class<?> callerClass, Consumer<? super BootstrapClassLoader> action) {
        TL.update(callerClass, action);
    }

    /**  */
    public static class BootstrapClassLoader {

    }

    /** Accessed by {@link AccessibleFieldHook.Bootstrap}. */
    public final class BootstrapContext {

        boolean disabled;

        /** The underlying field processor, which takes care of all static information. */
        public final FieldProcessor processor;

        BootstrapContext(FieldProcessor processor) {
            this.processor = requireNonNull(processor);
        }

        public void disable() {
            disabled = true;
        }
    }
}
