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
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor.VariableProcessor;
import zandbox.internal.hooks2.bootstrap.VarModel.Settings;
import zandbox.packed.hooks.AccessibleFieldHook;
import zandbox.packed.hooks.InjectAnnotatedVariableHook;

public class InjectableVariableBootstrapModel extends AbstractBootstrapModel {

    private static final ThreadLocal<InjectAnnotatedVariableHook> ANNOTATION = new ThreadLocal<>();

    /** A MethodHandle that can invoke {@link BeanField#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), InjectAnnotatedVariableHook.Bootstrap.class,
            "bootstrap", void.class);

    private static final ClassValue<InjectableVariableBootstrapModel> MODELS = new ClassValue<>() {

        @Override
        protected InjectableVariableBootstrapModel computeValue(Class<?> type) {
            InjectAnnotatedVariableHook afh = ANNOTATION.get();
            ExtensionModel.of(afh.extension()); // checks that it is a valid extension..
            assert afh.bootstrapBean() == type;
            Builder loader = LOADER.initializeClass(type);

            ClassBootstrapProcessor.Builder b = new ClassBootstrapProcessor.Builder(type);

            // System.out.println("Scanning bootstrap class " + type);

            b.build();

            return new InjectableVariableBootstrapModel(afh.bootstrapBean(), loader, afh.extension());
        }
    };

    private static final TmpLoader<Builder> LOADER = new TmpLoader<>(Builder.class, t -> new Builder());

    /** A VarHandle that can access {@link BeanField#processor}. */
    private static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), InjectAnnotatedVariableHook.Bootstrap.class,
            "context", InjectableVariableBootstrapModel.BootstrapContext.class);

    private final Settings request;

    private InjectableVariableBootstrapModel(Class<? extends InjectAnnotatedVariableHook.Bootstrap> bootstrapClass, Builder loader,
            Class<? extends Extension<?>> extensionClass) {
        super(bootstrapClass, extensionClass);
        this.request = new Settings(loader);
    }

    /** {@inheritDoc} */
    @Override
    public void bootstrapField(ClassBootstrapProcessor.FieldProcessor processor) {
        BootstrapContext b = new BootstrapContext(processor.toVariable(request));

        InjectAnnotatedVariableHook.Bootstrap instance = newInstance();

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

        // System.out.println("Created a hook on Injectable variable " + processor.expose(getClass().getModule()));
        // invoke runner
    }

    /**
     * Creates a bootstrap model from a {@link InjectAnnotatedVariableHook} annotation.
     * 
     * @param annotation
     *            the annotation to create a model for
     * @return the new model
     */
    public static InjectableVariableBootstrapModel of(InjectAnnotatedVariableHook annotation) {
        Class<? extends InjectAnnotatedVariableHook.Bootstrap> bootstrapClass = annotation.bootstrapBean();

        // The main issue here is that we want to store the extension
        // We use a ThreadLocal to pass the extension from boostrapClass to the class value.
        // We do this because we need to make sure that
        InjectableVariableBootstrapModel model;
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
     * A context class that is created for every bootstrap instance and accessed from {@link AccessibleFieldHook.Bootstrap}.
     */
    public final class BootstrapContext {

        boolean disabled;

        MethodHandle mh;
        
        /** The variable processor containers all static information of the variable. */
        public final VariableProcessor processor;

        BootstrapContext(VariableProcessor processor) {
            this.processor = requireNonNull(processor);
        }

        public void disable() {
            disabled = true;
        }
    }

    /**  */
    public static class Builder {
        public boolean supportTypeVariables;
        public boolean supportWildcardTypes;

        /**
         * Invoked by the various static initializer methods on {@link InjectAnnotatedVariableHook.Bootstrap}.
         * 
         * @param callerClass
         *            the calling class (must a proper subclass of {@link AccessibleFieldHook.Bootstrap})
         * @param action
         *            the action to invoke on the bootstrap class loader
         */
        public static void staticInitialize(Class<?> callerClass, Consumer<? super Builder> action) {
            LOADER.update(callerClass, action);
        }
    }
}
