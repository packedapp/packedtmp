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
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.IdentityHashMap;

import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.AnnotatedTypeHook;
import app.packed.container.extension.HookAggregateBuilder;
import app.packed.container.extension.OnHook;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TypeUtil;
import packed.internal.util.types.TypeVariableExtractor;

/**
 * An {@link OnHookAggregateBuilderModel} wraps
 */
final class OnHookAggregateBuilderModel {

    /** A cache of information for aggregator types. */
    private static final ClassValue<OnHookAggregateBuilderModel> MODEL_CACHE = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected OnHookAggregateBuilderModel computeValue(Class<?> type) {
            return new OnHookAggregateBuilderModel.Builder((Class<? extends HookAggregateBuilder<?>>) type).build();
        }
    };

    /** The type of aggregator. */
    private final Class<?> aggregatorType;

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    /** A constructor for creating new aggregator instance. */
    private final MethodHandle constructor;

    /** The type of result the aggregator produces. */
    private final Class<?> resultType;

    /**
     * Creates a new aggregator from the specified builder.
     * 
     * @param builder
     *            the builder to create an aggregator from
     */
    private OnHookAggregateBuilderModel(Builder builder) {
        this.constructor = requireNonNull(builder.constructor);
        this.aggregatorType = builder.aggregatorType;
        this.resultType = builder.resultType;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedFields = builder.annotatedFields;
        this.annotatedTypes = builder.annotatedTypes;
    }

    void invokeOnHook(HookAggregateBuilder<?> aggregator, AnnotatedFieldHook<?> hook) {
        if (aggregator.getClass() != aggregatorType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + aggregatorType + ", but was " + aggregator.getClass());
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

    void invokeOnHook(HookAggregateBuilder<?> aggregator, AnnotatedMethodHook<?> hook) {
        if (aggregator.getClass() != aggregatorType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + aggregatorType + ", but was " + aggregator.getClass());
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

    void invokeOnHook(HookAggregateBuilder<?> aggregator, AnnotatedTypeHook<?> hook) {
        if (aggregator.getClass() != aggregatorType) {
            throw new IllegalArgumentException("Must be specify an aggregator of type " + aggregatorType + ", but was " + aggregator.getClass());
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new aggregator (supplier) object.
     * 
     * @return a new aggregator object
     */
    HookAggregateBuilder<?> newAggregatorInstance() {
        try {
            return (HookAggregateBuilder<?>) constructor.invoke();
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns the type of result the aggregator produces.
     * 
     * @return the type of result the aggregator produces
     */
    final Class<?> resultType() {
        return resultType;
    }

    public static OnHookAggregateBuilderModel get(Class<? extends HookAggregateBuilder<?>> clazz) {
        return MODEL_CACHE.get(clazz);
    }

    private static class Builder {

        /** An type variable extractor to extract the type of pipeline the extension wirelet needs. */
        private static final TypeVariableExtractor AGGREGATE_BUILDER_TV_EXTRACTOR = TypeVariableExtractor.of(HookAggregateBuilder.class);

        private final Class<? extends HookAggregateBuilder<?>> aggregatorType;

        final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields = new IdentityHashMap<>();

        final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods = new IdentityHashMap<>();

        final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes = new IdentityHashMap<>();

        private MethodHandle constructor;

        final Class<?> resultType;

        @SuppressWarnings({ "rawtypes" })
        private Builder(Class<? extends HookAggregateBuilder<?>> aggregatorType) {
            this.aggregatorType = requireNonNull(aggregatorType);
            this.resultType = (Class) AGGREGATE_BUILDER_TV_EXTRACTOR.extract(aggregatorType);
        }

        private void addHookMethod(Lookup lookup, Method method, Parameter p) {
            // if (method.getParameterCount() != 1) {
            // throw new InvalidDeclarationException(
            // "Methods annotated with @OnHook on hook aggregates must have exactly one parameter, method = " +
            // StringFormatter.format(method));
            // }
            //
            // Parameter p = method.getParameters()[0];
            Class<?> cl = p.getType();

            if (cl == AnnotatedFieldHook.class) {
                addHookMethod0(lookup, method, p, annotatedFields);
            } else if (cl == AnnotatedMethodHook.class) {
                addHookMethod0(lookup, method, p, annotatedMethods);
            } else if (cl == AnnotatedTypeHook.class) {
                addHookMethod0(lookup, method, p, annotatedTypes);
            } else {
                throw new InvalidDeclarationException("Methods annotated with @OnHook on hook aggregates must have exactly one parameter of type "
                        + AnnotatedFieldHook.class.getSimpleName() + ", " + AnnotatedMethodHook.class.getSimpleName() + ", or"
                        + AnnotatedTypeHook.class.getSimpleName() + ", " + " for method = " + StringFormatter.format(method));
            }
        }

        private void addHookMethod0(MethodHandles.Lookup lookup, Method method, Parameter p,
                IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotations) {
            // if (ComponentClassDescriptor.Builder.METHOD_ANNOTATION_ACTIVATOR.get(annotationType) != type) {
            // throw new IllegalStateException("Annotation @" + annotationType.getSimpleName() + " must be annotated with @"
            // + Activate.class.getSimpleName() + "(" + extensionClass.getSimpleName() + ".class) to be used with this method");
            // }
            ParameterizedType pt = (ParameterizedType) p.getParameterizedType();
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationType = (Class<? extends Annotation>) pt.getActualTypeArguments()[0];

            if (annotations.containsKey(annotationType)) {
                throw new InvalidDeclarationException("There are multiple methods annotated with @OnHook on "
                        + StringFormatter.format(method.getDeclaringClass()) + " that takes " + p.getParameterizedType());
            }
            // Check that we have not added another previously for the same annotation

            MethodHandle mh;
            try {
                lookup = MethodHandles.privateLookupIn(aggregatorType, lookup);
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(aggregatorType) + ", the module '"
                        + aggregatorType.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }

            NativeImage.registerMethod(method);

            annotations.put(annotationType, mh);
        }

        OnHookAggregateBuilderModel build() {
            TypeUtil.checkClassIsInstantiable(aggregatorType);

            Constructor<?> constructor;
            try {
                constructor = aggregatorType.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(
                        "The extension " + StringFormatter.format(aggregatorType) + " must have a no-argument constructor to be installed.");
            }

            Lookup lookup = MethodHandles.lookup();
            try {
                lookup = MethodHandles.privateLookupIn(aggregatorType, lookup);
                this.constructor = lookup.unreflectConstructor(constructor);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the hook aggregate " + StringFormatter.format(aggregatorType) + ", the module '"
                        + aggregatorType.getModule().getName() + "' in which the class is located must be 'open' to 'app.packed.base'", e);
            }

            // Find all methods annotated with @OnHook
            for (Class<?> c = aggregatorType; c != Object.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    // Problemet er lidt hjaelpe metoder...
                    Parameter hook = null;
                    if (method.getParameterCount() > 0) {
                        for (Parameter p : method.getParameters()) {
                            Class<?> pc = p.getType();
                            if (pc == AnnotatedFieldHook.class || pc == AnnotatedMethodHook.class || pc == AnnotatedTypeHook.class) {
                                if (method.getParameterCount() != 1) {
                                    throw new InvalidDeclarationException("Methods on " + aggregatorType
                                            + " that takes a hook class must have exactly one parameter, method = " + StringFormatter.format(method));
                                }
                                hook = p;
                            }
                        }
                    }
                    if (hook != null) {
                        addHookMethod(lookup, method, hook);
                    }
                }
            }
            if (annotatedFields.isEmpty() && annotatedMethods.isEmpty() && annotatedTypes.isEmpty()) {
                throw new IllegalArgumentException("Hook aggregator '" + StringFormatter.format(aggregatorType)
                        + "' must define at least one method annotated with @" + OnHook.class.getSimpleName());
            }

            // Register the constructor if we are generating a native image
            NativeImage.registerConstructor(constructor);

            return new OnHookAggregateBuilderModel(this);
        }
    }
}
