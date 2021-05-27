package zandbox.internal.hooks2.repository;

import java.lang.annotation.Annotation;

import app.packed.base.Nullable;
import zandbox.internal.hooks2.bootstrap.AbstractBootstrapModel;
import zandbox.internal.hooks2.bootstrap.AccessibleFieldBootstrapModel;
import zandbox.internal.hooks2.bootstrap.InjectableVariableBootstrapModel;
import zandbox.packed.hooks.AccessibleFieldHook;
import zandbox.packed.hooks.AccessibleMethodHook;
import zandbox.packed.hooks.InjectAnnotatedVariableHook;

public abstract class AbstractCachedHookRepository implements HookRepository {

    /** A cache of hook models for annotations on fields. */
    private final ClassValue<AbstractBootstrapModel> fieldAnnotationModels = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        @Nullable
        protected AbstractBootstrapModel computeValue(Class<?> type) {
            AccessibleFieldHook fh = type.getAnnotation(AccessibleFieldHook.class);
            InjectAnnotatedVariableHook ivh = type.getAnnotation(InjectAnnotatedVariableHook.class);
            if (fh != null) {
                if (ivh != null) {
                    throw new Error(type + " cannot both be annotated with @FieldHook and @InjectableVariableHook");
                }
                return newFieldHookModel((Class<? extends Annotation>) type, fh);
            } else if (ivh != null) {
                return newInjectableVaritableHookModel((Class<? extends Annotation>) type, ivh);
            } else {
                return null;
            }
        }
    };

    /** A cache of hook models for annotations on fields. */
    private final ClassValue<AbstractBootstrapModel> methodAnnotationModels = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        @Nullable
        protected AbstractBootstrapModel computeValue(Class<?> type) {
            AccessibleMethodHook methodHook = type.getAnnotation(AccessibleMethodHook.class);
            InjectAnnotatedVariableHook ivh = type.getAnnotation(InjectAnnotatedVariableHook.class);
            if (methodHook != null) {
                if (ivh != null) {
                    throw new Error(type + " cannot both be annotated with @FieldHook and @InjectableVariableHook");
                }
                return newMethodHookModel((Class<? extends Annotation>) type, methodHook);
            } else if (ivh != null) {
                return newInjectableVaritableHookModel((Class<? extends Annotation>) type, ivh);
            } else {
                return null;
            }
        }
    };

    @Override
    public final @Nullable AbstractBootstrapModel lookupFieldAnnotation(Class<? extends Annotation> annotationType) {
        return fieldAnnotationModels.get(annotationType);
    }

    protected abstract AbstractBootstrapModel newMethodHookModel(Class<? extends Annotation> type, AccessibleMethodHook fh);

    @Override
    public final @Nullable AbstractBootstrapModel lookupMethodAnnotation(Class<? extends Annotation> annotationType) {
        return methodAnnotationModels.get(annotationType);
    }

    protected abstract AccessibleFieldBootstrapModel newFieldHookModel(Class<? extends Annotation> annotation, AccessibleFieldHook fh);

    protected abstract InjectableVariableBootstrapModel newInjectableVaritableHookModel(Class<? extends Annotation> annotation, InjectAnnotatedVariableHook ivh);


}
