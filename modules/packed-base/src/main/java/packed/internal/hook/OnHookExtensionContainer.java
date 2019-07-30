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
package packed.internal.hook;

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
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.OnHook;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Something
public class OnHookExtensionContainer {

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<OnHookExtensionContainer> CACHE = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected OnHookExtensionContainer computeValue(Class<?> type) {
            return new Builder((Class<? extends Extension>) type).build();
        }
    };

    final IdentityHashMap<Class<?>, MethodHandle> aggregators;

    /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods;

    /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
    final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes;

    private final Class<? extends Extension> extensionType;

    private OnHookExtensionContainer(Builder builder) {
        this.extensionType = builder.extensionType;
        this.aggregators = builder.aggregators;
        this.annotatedFields = builder.annotatedFields;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedTypes = builder.annotatedTypes;
    };

    @SuppressWarnings("unchecked")
    MethodHandle forAnnotatedField(ExtensionHookPerComponentGroup.Builder builder, PackedAnnotatedFieldHook<?> paf) {
        MethodHandle mh = annotatedFields.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException("" + paf.annotation().annotationType() + " for extension " + extensionType);
        }

        Class<?> owner = mh.type().parameterType(0);
        if (owner == extensionType) {
            builder.callbacks.add(new Callback(mh, paf));
        } else {
            OnHookAggregator a = OnHookAggregator.get((Class<? extends Supplier<?>>) owner);
            Supplier<?> sup = builder.mmm.computeIfAbsent(owner, k -> a.newAggregatorInstance());
            try {
                mh.invoke(sup, paf);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new RuntimeException(e);
            }
        }
        return mh;
    }

    @SuppressWarnings("unchecked")
    MethodHandle forAnnotatedMethod(ExtensionHookPerComponentGroup.Builder builder, PackedAnnotatedMethodHook<?> paf) {
        MethodHandle mh = annotatedMethods.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }

        Class<?> owner = mh.type().parameterType(0);
        if (owner == extensionType) {
            builder.callbacks.add(new Callback(mh, paf));
        } else {
            OnHookAggregator a = OnHookAggregator.get((Class<? extends Supplier<?>>) owner);
            Supplier<?> sup = builder.mmm.computeIfAbsent(owner, k -> a.newAggregatorInstance());
            try {
                mh.invoke(sup, paf);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new RuntimeException(e);
            }
        }
        return mh;
    }

    static OnHookExtensionContainer get(Class<? extends Extension> cl) {
        return CACHE.get(cl);
    }

    static class Builder {

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        final IdentityHashMap<Class<?>, MethodHandle> aggregators = new IdentityHashMap<>();

        /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields = new IdentityHashMap<>();

        /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods = new IdentityHashMap<>();

        /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes = new IdentityHashMap<>();

        private Builder(Class<? extends Extension> extensionType) {
            this.extensionType = requireNonNull(extensionType);
        }

        /**
         * @param lookup
         * @param method
         * @param oh
         */
        private void addHookMethod(Lookup lookup, Method method, OnHook oh) {

            if (method.getParameterCount() != 2) {
                throw new InvalidDeclarationException(
                        "Methods annotated with @OnHook on extensions must have exactly two parameter, method = " + StringFormatter.format(method));
            }
            if (method.getParameterTypes()[0] != ComponentConfiguration.class) {
                throw new InvalidDeclarationException("OOPS");
            }
            Parameter p = method.getParameters()[1];
            Class<?> cl = p.getType();

            Class<? extends Supplier<?>> aggregateType = oh.aggreateWith();

            if (aggregateType != OnHookAggregator.NoAggregator.class) {
                MethodHandle mh;
                try {
                    method.setAccessible(true);
                    mh = lookup.unreflect(method);
                } catch (IllegalAccessException | InaccessibleObjectException e) {
                    throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(extensionType) + ", the module '"
                            + extensionType.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
                }

                NativeImage.registerMethod(method);

                aggregators.put(aggregateType, mh);
                OnHookAggregator oha = OnHookAggregator.get(aggregateType);
                annotatedFields.putAll(oha.annotatedFields);
                annotatedMethods.putAll(oha.annotatedMethods);
                annotatedTypes.putAll(oha.annotatedTypes);
                // aggregators.p

                // Do something
            } else if (cl == AnnotatedFieldHook.class) {
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

            // TODO Auto-generated method stub

        }

        private void addHookMethod0(Lookup lookup, Method method, Parameter p, IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotations) {
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
                method.setAccessible(true);
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(extensionType) + ", the module '"
                        + extensionType.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }

            NativeImage.registerMethod(method);

            annotations.put(annotationType, mh);

        }

        private OnHookExtensionContainer build() {
            for (Class<?> c = extensionType; c != Extension.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    OnHook oh = method.getAnnotation(OnHook.class);
                    if (oh != null) {
                        addHookMethod(MethodHandles.lookup(), method, oh);
                    }
                }
            }

            return new OnHookExtensionContainer(this);
        }

    }
}

// Invocation

// @OnHook

// AnnotationTarget
//// Extension - @OnHook

// Future - Sidecars, @OnHook