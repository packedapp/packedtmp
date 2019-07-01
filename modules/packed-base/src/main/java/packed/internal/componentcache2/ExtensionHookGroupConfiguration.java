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
package packed.internal.componentcache2;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ContainerExtension;
import app.packed.container.ContainerExtensionActivator;
import app.packed.container.ContainerExtensionHookProcessor;
import app.packed.container.NativeImage;
import app.packed.hook.OnHook;
import app.packed.util.IllegalAccessRuntimeException;
import packed.internal.util.StringFormatter;
import packed.internal.util.TypeVariableExtractorUtil;

/**
 *
 */
public class ExtensionHookGroupConfiguration {

    static final ClassValue<ExtensionHookGroupConfiguration> FOR_CLASS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected ExtensionHookGroupConfiguration computeValue(Class<?> type) {
            return new ExtensionHookGroupConfiguration.Builder((Class<? extends ContainerExtensionHookProcessor<?>>) type).build();
        }
    };
    final HashMap<Class<? extends Annotation>, OnMethod> annotatedMethods;

    final MethodHandle mh;

    final Class<? extends ContainerExtension<?>> extensionClass;

    private ExtensionHookGroupConfiguration(Builder builder) {
        this.mh = requireNonNull(builder.mh);
        extensionClass = builder.extensionClass;
        this.annotatedMethods = builder.annotatedMethods;
    }

    public ContainerExtensionHookProcessor<?> instantiate() {
        try {
            return (ContainerExtensionHookProcessor<?>) mh.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void invokeAnnotatedMethod(Class<? extends Annotation> an, ContainerExtensionHookProcessor<?> p, AnnotatedMethodHook<?> hook) {
        OnMethod om = annotatedMethods.get(an);
        if (an == null) {
            throw new IllegalStateException();
        }
        try {
            om.mh.invoke(p, hook);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        final HashMap<Class<? extends Annotation>, OnMethod> annotatedMethods = new HashMap<>();

        private final Class<? extends ContainerExtensionHookProcessor<?>> type;

        final Class<? extends ContainerExtension<?>> extensionClass;

        MethodHandle mh;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Builder(Class<? extends ContainerExtensionHookProcessor<?>> type) {
            this.type = requireNonNull(type);
            extensionClass = (Class) TypeVariableExtractorUtil.findTypeParameterFromSuperClass(type, ContainerExtensionHookProcessor.class, 0);
        }

        @SuppressWarnings("rawtypes")
        ExtensionHookGroupConfiguration build() {
            if ((Class) type == ContainerExtensionHookProcessor.class) {
                throw new IllegalArgumentException();
            }
            // TODO check not abstract...
            Constructor<?> constructor;
            try {
                constructor = type.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("The extension " + StringFormatter.format(type) + " must have a no-argument constructor to be installed.");
            }

            Lookup lookup = MethodHandles.lookup();
            try {
                constructor.setAccessible(true);
                mh = lookup.unreflectConstructor(constructor);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(type) + ", the module '"
                        + type.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }

            NativeImage.registerConstructor(constructor);

            for (Class<?> c = type; c != Object.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(OnHook.class)) {
                        Parameter[] ps = method.getParameters();
                        if (ps.length != 1) {
                            throw new RuntimeException();
                        }
                        Parameter p = ps[0];
                        Class<?> cl = p.getType();
                        if (cl == AnnotatedMethodHook.class) {
                            @SuppressWarnings("unchecked")
                            Class<? extends Annotation> pt = (Class<? extends Annotation>) p.getParameterizedType();
                            addAnnotatedMethodHook(lookup, method, pt);
                        } else {
                            throw new UnsupportedOperationException("Unknown type " + cl);
                        }
                        // p.getParameterizedType()
                        // method.
                    }
                }
            }
            return new ExtensionHookGroupConfiguration(this);
        }

        private void addAnnotatedMethodHook(MethodHandles.Lookup lookup, Method method, Class<? extends Annotation> annotationType) {
            if (ComponentClassDescriptor.Builder.METHOD_ANNOTATION_ACTIVATOR.get(annotationType) != type) {
                throw new IllegalStateException("Annotation @" + annotationType.getSimpleName() + " must be annotated with @"
                        + ContainerExtensionActivator.class.getSimpleName() + "(" + extensionClass.getSimpleName() + ".class) to be used with this method");
            }
            MethodHandle mh;
            try {
                method.setAccessible(true);
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(type) + ", the module '"
                        + type.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }
            annotatedMethods.put(annotationType, new OnMethod(annotationType, mh));
        }
    }

    static class OnMethod {
        final Class<?> annotationType;
        final MethodHandle mh;

        public OnMethod(Class<?> annotationType, MethodHandle mh) {
            this.annotationType = annotationType;
            this.mh = mh;
        }
    }

}
