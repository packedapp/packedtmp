package zandbox.internal.hooks2.repository;

import app.packed.base.Nullable;
import zandbox.internal.hooks2.bootstrap.AbstractBootstrapModel;
import zandbox.internal.hooks2.bootstrap.AccessibleFieldBootstrapModel;
import zandbox.internal.hooks2.bootstrap.InjectableVariableBootstrapModel;
import zandbox.packed.hooks.AccessibleFieldHook;
import zandbox.packed.hooks.InjectAnnotatedVariableHook;

public class DefaultCaches {

    static class FieldAnnotationModelCache extends ClassValue<@Nullable AbstractBootstrapModel> {

        /** The default instance. Will main models as long as the model's annotation type is reachable. */
        static final FieldAnnotationModelCache DEFAULT = new FieldAnnotationModelCache();

        @Override
        protected @Nullable AbstractBootstrapModel computeValue(Class<?> type) {
            AccessibleFieldHook afh = type.getAnnotation(AccessibleFieldHook.class);
            InjectAnnotatedVariableHook ivh = type.getAnnotation(InjectAnnotatedVariableHook.class);
            if (afh != null) {
                if (ivh != null) {
                    throw new Error(type + " cannot both be annotated with @" + AccessibleFieldHook.class.getSimpleName() + " and @"
                            + InjectAnnotatedVariableHook.class.getSimpleName());
                }
                return AccessibleFieldBootstrapModel.of(afh);
            } else if (ivh != null) {
                return InjectableVariableBootstrapModel.of(ivh);
            } else {
                return null;
            }
        }
    }
}
