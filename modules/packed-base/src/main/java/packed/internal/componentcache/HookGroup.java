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
package packed.internal.componentcache;

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
import java.util.HashMap;

import app.packed.container.AnnotatedFieldHook;
import app.packed.container.AnnotatedMethodHook;
import app.packed.container.ContainerExtension;
import app.packed.container.ContainerExtensionActivator;
import app.packed.container.ContainerExtensionHookProcessor;
import app.packed.container.NativeImage;
import app.packed.hook.OldAnnotatedFieldHook;
import app.packed.hook.OnHook;
import app.packed.util.IllegalAccessRuntimeException;
import packed.internal.util.StringFormatter;
import packed.internal.util.TypeVariableExtractorUtil;

/**
 *
 */
final class HookGroup {

    static final ClassValue<HookGroup> FOR_CLASS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected HookGroup computeValue(Class<?> type) {
            return new HookGroup.Builder((Class<? extends ContainerExtensionHookProcessor<?>>) type).build();
        }
    };

    private final HashMap<Class<? extends Annotation>, HookMethod> annotatedMethods;

    final HashMap<Class<? extends Annotation>, HookMethod> annotatedFields;

    final MethodHandle mh;

    final Class<? extends ContainerExtension<?>> extensionClass;

    private HookGroup(Builder builder) {
        this.mh = requireNonNull(builder.mh);
        this.extensionClass = builder.extensionClass;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedFields = builder.annotatedFields;
    }

    ContainerExtensionHookProcessor<?> instantiate() {
        try {
            return (ContainerExtensionHookProcessor<?>) mh.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void invokeHookOnAnnotatedField(Class<? extends Annotation> an, ContainerExtensionHookProcessor<?> p, AnnotatedFieldHook<?> hook) {
        requireNonNull(p);
        HookMethod om = annotatedFields.get(an);
        if (om == null) {
            System.out.println(an);
            System.out.println(annotatedFields.keySet());
            throw new IllegalStateException("" + an);
        }

        try {
            om.mh.invoke(p, hook);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void invokeHookOnAnnotatedMethod(Class<? extends Annotation> an, ContainerExtensionHookProcessor<?> p, AnnotatedMethodHook<?> hook) {
        requireNonNull(p);
        HookMethod om = annotatedMethods.get(an);
        if (om == null) {
            System.out.println(an);
            System.out.println(annotatedMethods.keySet());
            throw new IllegalStateException("" + an);
        }

        try {
            om.mh.invoke(p, hook);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static class Builder {
        final HashMap<Class<? extends Annotation>, HookMethod> annotatedMethods = new HashMap<>();

        final HashMap<Class<? extends Annotation>, HookMethod> annotatedFields = new HashMap<>();

        private final Class<? extends ContainerExtensionHookProcessor<?>> type;

        final Class<? extends ContainerExtension<?>> extensionClass;

        MethodHandle mh;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Builder(Class<? extends ContainerExtensionHookProcessor<?>> type) {
            this.type = requireNonNull(type);
            extensionClass = (Class) TypeVariableExtractorUtil.findTypeParameterFromSuperClass(type, ContainerExtensionHookProcessor.class, 0);
        }

        @SuppressWarnings("rawtypes")
        HookGroup build() {
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
                        addMethod(lookup, method);
                    }
                }
            }
            return new HookGroup(this);
        }

        private void addMethod(Lookup lookup, Method method) {
            Parameter[] ps = method.getParameters();
            if (ps.length != 1) {
                throw new RuntimeException();
            }
            Parameter p = ps[0];
            Class<?> cl = p.getType();
            if (cl == AnnotatedMethodHook.class || cl == OldAnnotatedFieldHook.class) {
                ParameterizedType pt = (ParameterizedType) p.getParameterizedType();
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> anno = (Class<? extends Annotation>) pt.getActualTypeArguments()[0];
                HookMethod om = addAnnotatedMethodHook(lookup, method, anno);
                if (cl == AnnotatedMethodHook.class) {
                    annotatedMethods.put(anno, om);
                } else {
                    annotatedFields.put(anno, om);
                }

            } else {
                throw new UnsupportedOperationException("Unknown type " + cl);
            }
        }

        private HookMethod addAnnotatedMethodHook(MethodHandles.Lookup lookup, Method method, Class<? extends Annotation> annotationType) {
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
            return new HookMethod(annotationType, mh);
        }
    }

    // Egentlig har vi kun brug for MethodHandle, men vi gemmer den lige lidt endnu.
    static class HookMethod {
        final Class<?> annotationType;
        final MethodHandle mh;

        HookMethod(Class<?> annotationType, MethodHandle mh) {
            this.annotationType = annotationType;
            this.mh = mh;
        }
    }
}
