package packed.internal.bean.hooks.usesite;

import java.lang.annotation.Annotation;

import app.packed.base.Nullable;
import app.packed.hooks.BeanField;
import app.packed.hooks.BeanMethod;
import packed.internal.bean.hooks.FieldHookModel;
import packed.internal.bean.hooks.MethodHookBootstrapModel;
import packed.internal.container.ExtensionModel;
import packed.internal.invoke.OpenClass;

public class BootstrappedSourcedClassModel {

    /**
     * Creates a new component model instance.
     * 
     * @param realm
     *            a model of the container source that is trying to install the component
     * @param oc
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static HookModel newModel(OpenClass oc, @Nullable ExtensionModel extension) {
        return new Builder(oc, extension).build();
    }

    private static class Builder extends HookModel.Builder {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<MethodHookBootstrapModel> METHOD_ANNOTATIONS = new ClassValue<>() {

            @Override
            protected MethodHookBootstrapModel computeValue(Class<?> type) {
                BeanMethod.Hook ams = type.getAnnotation(BeanMethod.Hook.class);
                return ams == null ? null : new MethodHookBootstrapModel.Builder(ams).build();
            }
        };


        /** A cache of any extensions a particular annotation activates. */
        static final ClassValue<FieldHookModel> FIELD_ANNOTATIONS = new ClassValue<>() {

            @Override
            protected FieldHookModel computeValue(Class<?> type) {
                BeanField.Hook afs = type.getAnnotation(BeanField.Hook.class);
                return afs == null ? null : new FieldHookModel.Builder(afs).build();
            }
        };
        
        private Builder(OpenClass cp, @Nullable ExtensionModel extension) {
            super(cp, extension);
        }

        protected @Nullable MethodHookBootstrapModel getMethodModel(Class<? extends Annotation> annotationType) {
            return METHOD_ANNOTATIONS.get(annotationType);
        }

        @Override
        protected @Nullable FieldHookModel getFieldModel(Class<? extends Annotation> annotationType) {
            return FIELD_ANNOTATIONS.get(annotationType);
        }
    }
}
