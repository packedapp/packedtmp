package packed.internal.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.IdentityHashMap;

import app.packed.container.extension.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;

/** This class contains information about {@link OnHookGroup} methods for an extension type. */
public final class OnHookGroupModel {

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedTypeHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    /** The extension type we manage information for. */
    private final Class<? extends Extension> extensionType;

    /** A map of all methods that take a aggregator result object. Is always located on the actual extension. */
    public final IdentityHashMap<Class<?>, HGBModel> groups;

    /**
     * Creates a new group model.
     * 
     * @param builder
     *            the builder to create the model from
     */
    public OnHookGroupModel(HookClassBuilder builder, Class<? extends Extension> extensionType) {
        this.extensionType = requireNonNull(extensionType);
        this.groups = builder.groups;
        this.annotatedFields = builder.annotatedFields;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedTypes = builder.annotatedTypes;
    }

    public MethodHandle findMethodHandleForAnnotatedField(AnnotatedFieldHook<?> hook) {
        MethodHandle mh = annotatedFields.get(hook.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException(
                    "Extension " + extensionType + " does not know how to process fields annotated with " + hook.annotation().annotationType());
        }
        return mh;
    }

    public MethodHandle findMethodHandleForAnnotatedMethod(AnnotatedMethodHook<?> hook) {
        MethodHandle mh = annotatedMethods.get(hook.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }
        return mh;
    }

    public MethodHandle findMethodHandleForAnnotatedType(AnnotatedTypeHook<?> hook) {
        MethodHandle mh = annotatedTypes.get(hook.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }
        return mh;
    }
}