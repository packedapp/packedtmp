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
import java.util.IdentityHashMap;

import app.packed.container.Extension;
import app.packed.container.ExtensionHookProcessor;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.NativeImage;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.TypeVariableExtractorUtil;

/**
 *
 */
final class HookGroup {

    static final ClassValue<HookGroup> FOR_CLASS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected HookGroup computeValue(Class<?> type) {
            return new HookGroup.Builder((Class<? extends ExtensionHookProcessor<?>>) type).build();
        }
    };

    final IdentityHashMap<Class<? extends Annotation>, HookMethod> annotatedFields;

    private final IdentityHashMap<Class<? extends Annotation>, HookMethod> annotatedMethods;

    final Class<? extends Extension> extensionClass;

    final MethodHandle mh;

    private HookGroup(Builder builder) {
        this.mh = requireNonNull(builder.mh);
        this.extensionClass = builder.extensionClass;
        this.annotatedMethods = builder.annotatedMethods;
        this.annotatedFields = builder.annotatedFields;
    }

    ExtensionHookProcessor<?> instantiate() {
        try {
            return (ExtensionHookProcessor<?>) mh.invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void invokeHookOnAnnotatedField(ExtensionHookProcessor<?> p, AnnotatedFieldHook<?> hook) {
        requireNonNull(p);
        Class<? extends Annotation> an = hook.annotation().annotationType();
        HookMethod om = annotatedFields.get(an);
        if (om == null) {
            System.out.println(an);
            System.out.println(annotatedFields.keySet());
            throw new IllegalStateException("" + an);
        }

        try {
            om.mh.invoke(p, hook);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new RuntimeException(e);
        }
    }

    void invokeHookOnAnnotatedMethod(Class<? extends Annotation> an, ExtensionHookProcessor<?> p, AnnotatedMethodHook<?> hook) {
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
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new RuntimeException(e);
        }
    }

    static class Builder {
        final IdentityHashMap<Class<? extends Annotation>, HookMethod> annotatedFields = new IdentityHashMap<>();

        final IdentityHashMap<Class<? extends Annotation>, HookMethod> annotatedMethods = new IdentityHashMap<>();

        final Class<? extends Extension> extensionClass;

        MethodHandle mh;

        private final Class<? extends ExtensionHookProcessor<?>> type;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Builder(Class<? extends ExtensionHookProcessor<?>> type) {
            this.type = requireNonNull(type);
            extensionClass = (Class) TypeVariableExtractorUtil.findTypeParameterFromSuperClass(type, ExtensionHookProcessor.class, 0);
        }

        private HookMethod addAnnotatedMethodHook(MethodHandles.Lookup lookup, Method method, Class<? extends Annotation> annotationType) {
            // if (ComponentClassDescriptor.Builder.METHOD_ANNOTATION_ACTIVATOR.get(annotationType) != type) {
            // throw new IllegalStateException("Annotation @" + annotationType.getSimpleName() + " must be annotated with @"
            // + Activate.class.getSimpleName() + "(" + extensionClass.getSimpleName() + ".class) to be used with this method");
            // }
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

        private void addMethod(Lookup lookup, Method method) {
            Parameter[] ps = method.getParameters();
            if (ps.length != 1) {
                throw new RuntimeException();
            }
            Parameter p = ps[0];
            Class<?> cl = p.getType();
            if (cl == AnnotatedMethodHook.class || cl == AnnotatedFieldHook.class) {
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

        @SuppressWarnings("rawtypes")
        HookGroup build() {
            if ((Class) type == ExtensionHookProcessor.class) {
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
