package packed.internal.hooks2;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.hooks.OldFieldHook;
import app.packed.hooks2.AccessibleFieldHook;
import packed.internal.hooks.usesite.UseSiteFieldHookModel;
import packed.internal.hooks2.HookUsingClass.FieldProcessor;
import packed.internal.util.LookupUtil;

public class FieldHookModel implements AnnotatedFieldHookModel {

    final BootstrapModel model;

    final ClassValue<BootstrapModel> COMPONENT_MODEL = new ClassValue<>() {

        @Override
        protected BootstrapModel computeValue(Class<?> type) {
            // TODO Auto-generated method stub
            return null;
        }
    };

    FieldHookModel(BootstrapModel bootstrap) {
        this.model = requireNonNull(bootstrap);
    }

    @Override
    public void bootstrapAnnotatedField(FieldProcessor builder, Annotation activatingAnnotation) {
        Bootstrap b = new Bootstrap(builder);

        // invoke runner
    }

    public static FieldHookModel of(HookUseType hut, Class<? extends Annotation> annotation, AccessibleFieldHook fh) {
        Class<? extends AccessibleFieldHook.Bootstrap> bootstrapClass = fh.bootstrap();
        throw new UnsupportedOperationException();
    }

    /** Accessed by {@link AccessibleFieldHook.Bootstrap}. */
    public final class Bootstrap {

        /** The underlying field processor, which takes care of all static information. */
        public final FieldProcessor processor;

        Bootstrap(FieldProcessor processor) {
            this.processor = requireNonNull(processor);
        }

        public void disable() {
            // TODO Auto-generated method stub
        }
    }

    static record BootstrapModel(MethodHandle mhConstructor /* ()FieldHook.Bootstrap */ ) {

        /** A MethodHandle that can invoke {@link OldFieldHook.Bootstrap#model}. */
        static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OldFieldHook.Bootstrap.class, "bootstrap",
                void.class);

        /** A VarHandle that can access {@link OldFieldHook.Bootstrap#processor}. */
        static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), OldFieldHook.Bootstrap.class, "builder",
                UseSiteFieldHookModel.Builder.class);
    }

    /**  */
    public static class BootstrapModelClassInitialize {}
}
