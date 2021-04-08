package packed.internal.hooks.usesite;

import java.lang.annotation.Annotation;

import app.packed.base.Nullable;
import app.packed.hooks.MethodHook;
import packed.internal.container.ExtensionModel;
import packed.internal.hooks.MethodHookBootstrapModel;
import packed.internal.invoke.OpenClass;

public class SourcedHookedClassModel {

    /**
     * Creates a new component model instance.
     * 
     * @param realm
     *            a model of the container source that is trying to install the component
     * @param oc
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static HookedClassModel newModel(OpenClass oc, @Nullable ExtensionModel extension) {
        return new Builder(oc, extension).build();
    }

    private static class Builder extends HookedClassModel.Builder {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodHookBootstrapModel> EXTENSION_METHOD_ANNOTATION = new ClassValue<>() {

            @Override
            protected MethodHookBootstrapModel computeValue(Class<?> type) {
                MethodHook ams = type.getAnnotation(MethodHook.class);
                return ams == null ? null : new MethodHookBootstrapModel.Builder(ams).build();
            }
        };

        private Builder(OpenClass cp, @Nullable ExtensionModel extension) {
            super(HookUseSite.COMPONENT_SOURCE, cp, extension);
        }

        protected @Nullable MethodHookBootstrapModel getMethodModel(Class<? extends Annotation> annotationType) {
            return EXTENSION_METHOD_ANNOTATION.get(annotationType);
        }
    }
}
