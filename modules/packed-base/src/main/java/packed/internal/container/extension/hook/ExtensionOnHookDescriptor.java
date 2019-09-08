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

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.AnnotatedTypeHook;
import app.packed.container.extension.Extension;
import app.packed.container.extension.HookGroupBuilder;
import app.packed.container.extension.OnHookGroup;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;

/** This class contains information about {@link OnHookGroup} methods for an extension type. */
final class ExtensionOnHookDescriptor {

    /** A cache of descriptors for a particular extension type. */
    private static final ClassValue<ExtensionOnHookDescriptor> CACHE = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionOnHookDescriptor computeValue(Class<?> type) {
            return new Builder((Class<? extends Extension>) type).build();
        }
    };

    /** A map of all methods that take a aggregator result object. Is always located on the actual extension. */
    final IdentityHashMap<Class<?>, MethodHandle> aggregators;

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
    private ExtensionOnHookDescriptor(Builder builder) {
        this.extensionType = builder.extensionType;
        this.aggregators = builder.aggregators;
        this.annotatedFields = builder.annotatedFields;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedTypes = builder.annotatedTypes;
    }

    MethodHandle findMethodHandleForAnnotatedField(PackedAnnotatedFieldHook<?> paf) {
        MethodHandle mh = annotatedFields.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException(
                    "Extension " + extensionType + " does not know how to process fields annotated with " + paf.annotation().annotationType());
        }
        return mh;
    }

    MethodHandle findMethodHandleForAnnotatedMethod(PackedAnnotatedMethodHook<?> paf) {
        MethodHandle mh = annotatedMethods.get(paf.annotation().annotationType());
        if (mh == null) {
            throw new UnsupportedOperationException();
        }
        return mh;
    }

    /**
     * Returns a descriptor for the specified extensionType
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return the descriptor
     * @throws InvalidDeclarationException
     *             if the usage of {@link OnHookGroup} on the extension does not adhere to contract
     */
    static ExtensionOnHookDescriptor get(Class<? extends Extension> extensionType) {
        return CACHE.get(extensionType);
    }

    /** A builder for {@link ExtensionOnHookDescriptor}. */
    private static class Builder {

        final IdentityHashMap<Class<?>, MethodHandle> aggregators = new IdentityHashMap<>();

        /** A map of all methods that takes a {@link AnnotatedFieldHook}. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedFields = new IdentityHashMap<>();

        /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedMethods = new IdentityHashMap<>();

        /** A map of all methods that takes a {@link AnnotatedMethodHook}. */
        private final IdentityHashMap<Class<? extends Annotation>, MethodHandle> annotatedTypes = new IdentityHashMap<>();

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        private Builder(Class<? extends Extension> extensionType) {
            this.extensionType = requireNonNull(extensionType);
        }

        /**
         * @param lookup
         * @param method
         * @param oh
         */
        private void addHookMethod(Lookup lookup, Method method, OnHookGroup oh) {

            if (method.getParameterCount() != 2) {
                throw new InvalidDeclarationException(
                        "Methods annotated with @OnHook on extensions must have exactly two parameter, method = " + StringFormatter.format(method));
            }
            if (method.getParameterTypes()[0] != ComponentConfiguration.class) {
                throw new InvalidDeclarationException("OOPS");
            }
            Parameter p = method.getParameters()[1];
            Class<?> cl = p.getType();

            Class<? extends HookGroupBuilder<?>> aggregateType = oh.value();

            if (aggregateType != ExtensionHookPerComponentGroup.NoAggregator.class) {
                MethodHandle mh;
                try {
                    lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), lookup);
                    mh = lookup.unreflect(method);
                } catch (IllegalAccessException | InaccessibleObjectException e) {
                    throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(extensionType) + ", the module '"
                            + extensionType.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
                }

                NativeImage.registerMethod(method);

                aggregators.put(aggregateType, mh);
                HookGroupBuilderModel oha = HookGroupBuilderModel.of(aggregateType);
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
                lookup = MethodHandles.privateLookupIn(method.getDeclaringClass(), lookup);
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new UncheckedIllegalAccessException("In order to use the extension " + StringFormatter.format(extensionType) + ", the module '"
                        + extensionType.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }

            NativeImage.registerMethod(method);

            annotations.put(annotationType, mh);
        }

        private ExtensionOnHookDescriptor build() {
            for (Class<?> c = extensionType; c != Extension.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    OnHookGroup oh = method.getAnnotation(OnHookGroup.class);
                    if (oh != null) {
                        addHookMethod(MethodHandles.lookup(), method, oh);
                    }
                }
            }

            return new ExtensionOnHookDescriptor(this);
        }
    }
}
