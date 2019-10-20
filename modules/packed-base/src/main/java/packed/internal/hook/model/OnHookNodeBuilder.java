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
package packed.internal.hook.model;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.IdentityHashMap;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.container.access.ClassProcessor;
import packed.internal.util.ThrowableFactory;
import packed.internal.util.TypeUtil;

/**
 *
 */
// Lazy initialize maps...
class OnHookNodeBuilder {

    /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedFieldHook} as parameter. */
    final IdentityHashMap<Class<? extends Annotation>, Entry> annotatedFieldHooks = new IdentityHashMap<>();

    /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedMethodHook} as parameter. */
    final IdentityHashMap<Class<? extends Annotation>, Entry> annotatedMethodHooks = new IdentityHashMap<>();

    /** Fields annotated with {@link OnHook} taking a single {@link AnnotatedTypeHook} as parameter. */
    final IdentityHashMap<Class<? extends Annotation>, Entry> annotatedTypeHooks = new IdentityHashMap<>();

    /** Components that are of a specific type. */
    final IdentityHashMap<Class<?>, Entry> assignableToHooks = new IdentityHashMap<>();

    /** Non-base hooks */
    final IdentityHashMap<Class<?>, Entry> nonBaseHooks = new IdentityHashMap<>();

    // STEP 1
    /// Find All methods
    /// Validate Parameters
    /// Go into dependencies...

    // Find them, validate parameters
    // Validate we can make MethodHandle

    // Step 2
    // Validate no

    final OnHookSet s;

    final OnHookContainerType type;

    final ClassProcessor cp;

    final int id;

    OnHookNodeBuilder(OnHookSet s, int id, ClassProcessor cp, OnHookContainerType type) {
        this.s = requireNonNull(s);
        this.id = id;
        this.cp = requireNonNull(cp);
        this.type = requireNonNull(type);
    }

    final <T extends Throwable> void onMethod(Method method, ThrowableFactory<T> tf) throws T {
        if (!method.isAnnotationPresent(OnHook.class)) {
            return;
        }
        int hookIndex = -1;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (Hook.class.isAssignableFrom(parameters[i].getType())) {
                if (hookIndex != -1) {
                    throw tf.newThrowableForMethod("Cannot have more than 1 parameter that are instances of " + Hook.class.getCanonicalName(), method);
                }
                hookIndex = i;
            }
        }
        if (hookIndex == -1) {
            throw tf.newThrowableForMethod("Atleast one parameter most be an instance of " + Hook.class.getCanonicalName(), method);
        }

        // If we have additional parameters, check that they are okay.
        if (parameters.length > 1) {
            // Check that the remaining are okay
            // Probably want these additional parameters in a list to Entry
        }

        Parameter hook = parameters[hookIndex];
        MethodHandle mh = cp.unreflect(method, tf);
        Class<?> hookType = hook.getType();
        if (hookType == AnnotatedFieldHook.class) {
            process(hook, method, mh, annotatedFieldHooks);
        } else if (hookType == AnnotatedMethodHook.class) {
            process(hook, method, mh, annotatedMethodHooks);
        } else if (hookType == AnnotatedTypeHook.class) {
            process(hook, method, mh, annotatedTypeHooks);
        } else if (hookType == AssignableToHook.class) {
            process(hook, method, mh, assignableToHooks);
        } else {
            if (hookType == cp.clazz()) {
                tf.newThrowableForMethod("Hook cannot depend on itself", method);
            }
            TypeUtil.checkClassIsInstantiable(hookType);
            nonBaseHooks.compute(hookType, (k, v) -> new Entry(s.builderFor(hookType), method, mh, v));
        }
    }

    @SuppressWarnings("unchecked")
    private void process(Parameter p, Method method, MethodHandle mh, IdentityHashMap<?, Entry> map) {
        ParameterizedType pt = (ParameterizedType) p.getParameterizedType();
        Class<?> typeVariable = (Class<?>) pt.getActualTypeArguments()[0];
        ((IdentityHashMap<Class<?>, Entry>) map).compute(typeVariable, (k, v) -> new Entry(this, method, mh, v));
    }

    static class Entry {
        final Method method;
        final OnHookNodeBuilder builder;
        final MethodHandle methodHandle;

        @Nullable
        final Entry next;

        Entry(OnHookNodeBuilder builder, Method method, MethodHandle methodHandle, Entry next) {
            this.builder = requireNonNull(builder);
            this.method = requireNonNull(method);
            this.methodHandle = requireNonNull(methodHandle);
            this.next = next;
        }
    }
}
