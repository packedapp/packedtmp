package zandbox.internal.hooks2.bootstrap;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

import app.packed.container.Extension;
import app.packed.hooks.OldFieldHook;
import packed.internal.container.ExtensionModel;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TmpLoader;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor.MethodProcessor;
import zandbox.packed.hooks.AccessibleFieldHook;
import zandbox.packed.hooks.AccessibleMethodHook;

/** A model create from an AccessibleFieldHook annotation. */
public final class AccessibleMethodBootstrapModel extends AbstractBootstrapModel {

    private static final ThreadLocal<AccessibleMethodHook> ANNOTATION = new ThreadLocal<>();

    /** A MethodHandle that can invoke {@link OldFieldHook.Bootstrap#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), AccessibleMethodHook.Bootstrap.class,
            "bootstrap", void.class);

    private static final ClassValue<AccessibleMethodBootstrapModel> MODELS = new ClassValue<AccessibleMethodBootstrapModel>() {

        @Override
        protected AccessibleMethodBootstrapModel computeValue(Class<?> type) {
            AccessibleMethodHook afh = ANNOTATION.get();
            ExtensionModel.of(afh.extension()); // checks that it is a valid extension..
            assert afh.bootstrapBean() == type;
            BootstrapClassLoader loader = TL.initializeClass(type);

            ClassBootstrapProcessor.Builder b = new ClassBootstrapProcessor.Builder(type);

            //System.out.println("Scanning method bootstrap class " + type);

            b.build();

            return new AccessibleMethodBootstrapModel(afh.bootstrapBean(), loader, afh.extension());
        }
    };

    private static final TmpLoader<BootstrapClassLoader> TL = new TmpLoader<>(BootstrapClassLoader.class, t -> new BootstrapClassLoader());

    /** A VarHandle that can access {@link OldFieldHook.Bootstrap#processor}. */
    private static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), AccessibleMethodHook.Bootstrap.class,
            "context", AccessibleMethodBootstrapModel.BootstrapContext.class);

    private AccessibleMethodBootstrapModel(Class<? extends AccessibleMethodHook.Bootstrap> bootstrapClass, BootstrapClassLoader loader,
            Class<? extends Extension> extensionClass) {
        super(bootstrapClass, extensionClass);
    }

    /** {@inheritDoc} */
    @Override
    public void bootstrapMethod(ClassBootstrapProcessor.MethodProcessor processor) {
        BootstrapContext b = new BootstrapContext(processor);

        AccessibleMethodHook.Bootstrap instance = newInstance();

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

        //System.out.println("Created a hook on " + processor.expose(getClass().getModule()));
        // add something to
        // invoke runner
    }

    /**
     * Creates a bootstrap model from a {@link AccessibleFieldHook} annotation.
     * 
     * @param annotation
     *            the annotation to create a model for
     * @return the new model
     */
    public static AccessibleMethodBootstrapModel of(AccessibleMethodHook annotation) {
        Class<? extends AccessibleMethodHook.Bootstrap> bootstrapClass = annotation.bootstrapBean();
        // The main issue here is that we want to store the extension
        // We use a ThreadLocal to pass the extension from boostrapClass to the class value.
        // We do this because we need to make sure that
        AccessibleMethodBootstrapModel model;

        // Will recursively go into method
        AccessibleMethodHook current = ANNOTATION.get();
        ANNOTATION.set(annotation);
        try {
            model = MODELS.get(bootstrapClass);
        } finally {
            ANNOTATION.set(current);
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
        public final MethodProcessor processor;

        BootstrapContext(MethodProcessor processor) {
            this.processor = requireNonNull(processor);
        }

        public void disable() {
            disabled = true;
        }
    }
}
