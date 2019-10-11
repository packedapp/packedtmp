package packed.internal.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.OnHook;
import packed.internal.util.StringFormatter;

/** A model for classes that may contain methods annotated with {@link OnHook}. */
public final class HookContainerModel {

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    private final Map<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    private final Map<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedTypeHook}. */
    private final Map<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    /** The extension type we manage information for. */
    private final Class<?> containerType;

    /** A map of all methods that take a aggregator result object. Is always located on the actual extension. */
    final Map<Class<?>, MethodHandle> groups;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder to create the model from
     */
    private HookContainerModel(Builder builder) {
        this.containerType = requireNonNull(builder.containerType);
        this.groups = Map.copyOf(builder.hooks.groups);
        this.annotatedFields = Map.copyOf(builder.hooks.annotatedFields);
        this.annotatedMethods = Map.copyOf(builder.hooks.annotatedMethods);
        this.annotatedTypes = Map.copyOf(builder.hooks.annotatedTypes);
    }

    MethodHandle findMethodHandleForAnnotatedField(AnnotatedFieldHook<?> hook) {
        MethodHandle mh = annotatedFields.get(hook.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException(
                    StringFormatter.format(containerType) + " does not know how to process fields annotated with " + hook.annotation().annotationType());
        }
        return mh;
    }

    MethodHandle findMethodHandleForAnnotatedMethod(AnnotatedMethodHook<?> hook) {
        MethodHandle mh = annotatedMethods.get(hook.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }
        return mh;
    }

    MethodHandle findMethodHandleForAnnotatedType(AnnotatedTypeHook<?> hook) {
        MethodHandle mh = annotatedTypes.get(hook.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }
        return mh;
    }

    public static class Builder implements Consumer<Method> {

        /** The class that may contain methods annotated with {@link OnHook}. */
        private final Class<?> containerType;

        private final HookClassBuilder hooks;

        public Builder(Class<?> containerType) {
            this.containerType = requireNonNull(containerType);
            this.hooks = new HookClassBuilder(containerType, false);
        }

        /** {@inheritDoc} */
        @Override
        public void accept(Method method) {
            hooks.processMethod(method);
        }

        public HookContainerModel build() {
            return new HookContainerModel(this);
        }
    }
}