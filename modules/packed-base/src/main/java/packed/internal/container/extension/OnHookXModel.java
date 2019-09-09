package packed.internal.container.extension;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.IdentityHashMap;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.Extension;
import app.packed.container.extension.OnHookGroup;
import packed.internal.container.extension.hook.OnHookMemberProcessor;

/** This class contains information about {@link OnHookGroup} methods for an extension type. */
public final class OnHookXModel {

    /** A map of all methods that take a aggregator result object. Is always located on the actual extension. */
    public final IdentityHashMap<Class<?>, MethodHandle> groups;

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    /** The extension type we manage information for. */
    private final Class<? extends Extension> extensionType;

    /**
     * Creates a new manager from the specified builder.
     * 
     * @param builder
     *            the builder to create the manager from
     */
    @SuppressWarnings("unchecked")
    OnHookXModel(OnHookMemberProcessor builder) {
        this.extensionType = (Class<? extends Extension>) builder.actualType;
        this.groups = builder.groups;
        this.annotatedFields = builder.annotatedFields;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedTypes = builder.annotatedTypes;
    }

    public MethodHandle findMethodHandleForAnnotatedField(AnnotatedFieldHook<?> paf) {
        MethodHandle mh = annotatedFields.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException(
                    "Extension " + extensionType + " does not know how to process fields annotated with " + paf.annotation().annotationType());
        }
        return mh;
    }

    public MethodHandle findMethodHandleForAnnotatedMethod(AnnotatedMethodHook<?> paf) {
        MethodHandle mh = annotatedMethods.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }
        return mh;
    }
}