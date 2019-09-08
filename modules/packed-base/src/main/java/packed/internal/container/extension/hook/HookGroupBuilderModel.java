/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packed.internal.container.extension.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.IdentityHashMap;
import java.util.Map;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.AnnotatedTypeHook;
import app.packed.container.extension.HookGroupBuilder;
import app.packed.container.extension.OnHook;
import app.packed.container.extension.OnHookGroup;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.NativeImage;
import packed.internal.container.extension.AbstractFoo;
import packed.internal.reflect.ConstructorExtractor;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TypeUtil;

/**
 * An {@link HookGroupBuilderModel} wraps
 */
final class HookGroupBuilderModel extends AbstractFoo<HookGroupBuilder<?>> {

    /** A cache of information for aggregator types. */
    private static final ClassValue<HookGroupBuilderModel> MODEL_CACHE = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected HookGroupBuilderModel computeValue(Class<?> type) {
            return new HookGroupBuilderModel.Builder((Class<? extends HookGroupBuilder<?>>) type).build();
        }
    };

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final Map<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    /** The type of aggregator. */
    private final Class<?> builderType;

    /** The type of result the aggregator produces. */
    private final Class<?> groupType;

    /**
     * Creates a new model from the specified builder.
     * 
     * @param builder
     *            the builder to create a model from
     */
    private HookGroupBuilderModel(Builder builder) {
        super(builder.constructor);
        this.builderType = builder.builderType;
        this.groupType = builder.groupType;
        this.annotatedMethods = Map.copyOf(builder.annotatedMethods);
        this.annotatedFields = Map.copyOf(builder.annotatedFields);
        this.annotatedTypes = Map.copyOf(builder.annotatedTypes);
    }

    /**
     * Returns the type of group the builder produces.
     * 
     * @return the type of group the builder produces
     */
    final Class<?> groupType() {
        return groupType;
    }

    void invokeOnHook(HookGroupBuilder<?> aggregator, AnnotatedFieldHook<?> hook) {
        if (aggregator.getClass() != builderType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " + aggregator.getClass());
        }
        Class<? extends Annotation> an = hook.annotation().annotationType();

        MethodHandle om = annotatedFields.get(an);
        if (om == null) {
            // We will normally have checked for this previously
            throw new IllegalStateException("" + an);
        }

        try {
            om.invoke(aggregator, hook);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new RuntimeException(e);
        }
    }

    void invokeOnHook(HookGroupBuilder<?> aggregator, AnnotatedMethodHook<?> hook) {
        if (aggregator.getClass() != builderType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " + aggregator.getClass());
        }
        Class<? extends Annotation> an = hook.annotation().annotationType();

        MethodHandle om = annotatedMethods.get(an);
        if (om == null) {
            throw new IllegalStateException("" + an);
        }

        try {
            om.invoke(aggregator, hook);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new RuntimeException(e);
        }
    }

    void invokeOnHook(HookGroupBuilder<?> aggregator, AnnotatedTypeHook<?> hook) {
        if (aggregator.getClass() != builderType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + builderType + ", but was " + aggregator.getClass());
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a model for the specified hook group builder type.
     * 
     * @param type
     *            the type of hook group builder
     * @return a model for the specified group builder type
     */
    public static HookGroupBuilderModel of(Class<? extends HookGroupBuilder<?>> type) {
        return MODEL_CACHE.get(type);
    }

    /** A builder object, that extract relevant methods from a hook group builder. */
    private static class Builder {

        /** An type variable extractor to extract the type of hook group the builder produces. */
        private static final TypeVariableExtractor AGGREGATE_BUILDER_TV_EXTRACTOR = TypeVariableExtractor.of(HookGroupBuilder.class);

        /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedFieldHook} as parameter. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields = new IdentityHashMap<>();

        /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedMethodHook} as parameter. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods = new IdentityHashMap<>();

        /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedTypeHook} as parameter. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes = new IdentityHashMap<>();

        /** The type of hook group builder. */
        private final Class<? extends HookGroupBuilder<?>> builderType;

        /** A constructor used to create new hook group builders. */
        private MethodHandle constructor;

        /** The type of hook group the builder produces */
        private final Class<?> groupType;

        /**
         * Creates a new builder for the specified hook group builder type.
         * 
         * @param builderType
         *            the type of hook group builder
         */
        @SuppressWarnings({ "rawtypes" })
        private Builder(Class<? extends HookGroupBuilder<?>> builderType) {
            this.builderType = requireNonNull(builderType);
            this.groupType = (Class) AGGREGATE_BUILDER_TV_EXTRACTOR.extract(builderType);
        }

        private void addHookMethod(MethodHandles.Lookup lookup, Method method, Parameter p,
                IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotations) {
            // if (ComponentClassDescriptor.Builder.METHOD_ANNOTATION_ACTIVATOR.get(annotationType) != type) {
            // throw new IllegalStateException("Annotation @" + annotationType.getSimpleName() + " must be annotated with @"
            // + Activate.class.getSimpleName() + "(" + extensionClass.getSimpleName() + ".class) to be used with this method");
            // }
            ParameterizedType pt = (ParameterizedType) p.getParameterizedType();
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationType = (Class<? extends Annotation>) pt.getActualTypeArguments()[0];

            // Check that we have not added another previously for the same annotation
            if (annotations.containsKey(annotationType)) {
                throw new InvalidDeclarationException("There are multiple methods annotated with @OnHook on "
                        + StringFormatter.format(method.getDeclaringClass()) + " that takes " + p.getParameterizedType());
            }

            MethodHandle mh;
            try {
                lookup = MethodHandles.privateLookupIn(builderType, lookup);
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(builderType) + ", the module '"
                        + builderType.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }

            NativeImage.registerMethod(method);

            annotations.put(annotationType, mh);
        }

        HookGroupBuilderModel build() {
            TypeUtil.checkClassIsInstantiable(builderType);

            this.constructor = ConstructorExtractor.extract(builderType);

            Lookup lookup = MethodHandles.lookup();
            try {
                lookup = MethodHandles.privateLookupIn(builderType, lookup);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the hook aggregate " + StringFormatter.format(builderType) + ", the module '"
                        + builderType.getModule().getName() + "' in which the class is located must be 'open' to 'app.packed.base'", e);
            }

            // Find all methods annotated with @OnHook
            for (Class<?> c = builderType; c != Object.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    // Problemet er lidt hjaelpe metoder...
                    if (method.isAnnotationPresent(OnHook.class)) {
                        if (method.getParameterCount() != 1) {
                            throw new InvalidDeclarationException("Methods annotated with @" + OnHook.class.getSimpleName()
                                    + " on hook group builder must take exactly one parameter, method = " + StringFormatter.format(method));
                        }
                        Parameter p = method.getParameters()[0];
                        Class<?> pc = p.getType();
                        if (pc == AnnotatedFieldHook.class) {
                            addHookMethod(lookup, method, p, annotatedFields);
                        } else if (pc == AnnotatedMethodHook.class) {
                            addHookMethod(lookup, method, p, annotatedMethods);
                        } else if (pc == AnnotatedTypeHook.class) {
                            addHookMethod(lookup, method, p, annotatedTypes);
                        } else {
                            throw new InvalidDeclarationException("Methods annotated with @OnHook on hook aggregates must have exactly one parameter of type "
                                    + AnnotatedFieldHook.class.getSimpleName() + ", " + AnnotatedMethodHook.class.getSimpleName() + ", or"
                                    + AnnotatedTypeHook.class.getSimpleName() + ", " + " for method = " + StringFormatter.format(method));
                        }
                    }
                }
            }
            if (annotatedFields.isEmpty() && annotatedMethods.isEmpty() && annotatedTypes.isEmpty()) {
                throw new IllegalArgumentException("Hook aggregator '" + StringFormatter.format(builderType)
                        + "' must define at least one method annotated with @" + OnHookGroup.class.getSimpleName());
            }

            return new HookGroupBuilderModel(this);
        }
    }
}
