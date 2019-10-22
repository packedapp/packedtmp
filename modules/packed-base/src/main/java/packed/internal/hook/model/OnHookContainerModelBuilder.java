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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.reflect.ClassFinder;
import packed.internal.reflect.ClassProcessor;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.util.UncheckedThrowableFactory;
import packed.internal.util.types.TypeUtil;

/**
 *
 */
// Ideen er at vi resolver, maaske alle paa en gang

// Vi bliver noedt til at have den her som samler alt,
public final class OnHookContainerModelBuilder {

    /** Temporary builders. */
    private final IdentityHashMap<Class<?>, OnHookContainerNode> nodes = new IdentityHashMap<>();

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    IdentityHashMap<Class<? extends Annotation>, OnHookEntry> onHookAnnotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    IdentityHashMap<Class<? extends Annotation>, OnHookEntry> onHookAnnotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
    IdentityHashMap<Class<? extends Annotation>, OnHookEntry> onHookAnnotatedTypes;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    IdentityHashMap<Class<?>, OnHookEntry> onHookAssignableTos;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    IdentityHashMap<Class<?>, OnHookEntry> onHookCustomHooks;

    /** The root builder. */
    private final OnHookContainerNode root;

    final ArrayList<OnHookContainerNode> sorted = new ArrayList<>();

    private final ArrayDeque<OnHookContainerNode> unprocessedNodes = new ArrayDeque<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public OnHookContainerModelBuilder(ClassProcessor cp, Class<?>... additionalParameters) {
        if (Hook.class.isAssignableFrom(cp.clazz())) {
            this.root = newNode(cp, (Class<? extends Hook>) cp.clazz(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
        } else {
            // This cast is not valid... For example is Bundle not a hook.
            this.root = new OnHookContainerNode((Class) cp.clazz(), cp, null);
        }
    }

    private void onMethod(OnHookContainerNode b, Method method, UncheckedThrowableFactory<? extends RuntimeException> tf) {
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

        // If we have additional parameters on our initial builder, check that they are okay.
        if (b == root && parameters.length > 1) {
            // Check that the remaining are okay
            // Probably want these additional parameters in a list to Entry
        }

        Parameter hook = parameters[hookIndex];
        MethodHandle mh = b.cp.unreflect(method, tf);
        @SuppressWarnings("unchecked")
        Class<? extends Hook> hookType = (Class<? extends Hook>) hook.getType();
        if (hookType == AnnotatedFieldHook.class) {
            IdentityHashMap<Class<? extends Annotation>, OnHookEntry> o = onHookAnnotatedFields;
            if (o == null) {
                o = onHookAnnotatedFields = new IdentityHashMap<>(1);
            }
            process(b, hook, method, mh, o);
        } else if (hookType == AnnotatedMethodHook.class) {
            IdentityHashMap<Class<? extends Annotation>, OnHookEntry> o = onHookAnnotatedMethods;
            if (o == null) {
                o = onHookAnnotatedMethods = new IdentityHashMap<>(1);
            }
            process(b, hook, method, mh, o);
        } else if (hookType == AnnotatedTypeHook.class) {
            IdentityHashMap<Class<? extends Annotation>, OnHookEntry> o = onHookAnnotatedTypes;
            if (o == null) {
                o = onHookAnnotatedTypes = new IdentityHashMap<>(1);
            }
            process(b, hook, method, mh, o);
        } else if (hookType == AssignableToHook.class) {
            IdentityHashMap<Class<?>, OnHookEntry> o = onHookAssignableTos;
            if (o == null) {
                o = onHookAssignableTos = new IdentityHashMap<>(1);
            }
            process(b, hook, method, mh, o);
        } else {
            if (hookType == b.cp.clazz()) {
                tf.newThrowableForMethod("Hook cannot depend on itself", method);
            }
            TypeUtil.checkClassIsInstantiable(hookType);
            IdentityHashMap<Class<?>, OnHookEntry> n = onHookCustomHooks;
            if (n == null) {
                n = onHookCustomHooks = new IdentityHashMap<>(1);
            }
            onHookCustomHooks.compute(hookType, (k, v) -> {
                OnHookContainerNode node = nodes.computeIfAbsent(k, ignore -> {
                    Class<?> cl = ClassFinder.findDeclaredClass(hookType, "Builder", Hook.Builder.class);
                    ClassProcessor cp = root.cp.spawn(cl);
                    MethodHandle constructor = ConstructorFinder.find(cp, tf);

                    // TODO validate type variable
                    OnHookContainerNode newB = new OnHookContainerNode(hookType, cp, constructor);
                    unprocessedNodes.addLast(newB); // make sure it will be procesed at some point.
                    return newB;
                });

                // Test if the builder of a hooks depends on the hook itself
                if (b == node) {
                    throw tf.newThrowableForMethod("Hook cannot depend on itself", method);
                }

                // Or maybe we need to this for circles??
                // If we have pure tests
                if (b != root) {
                    b.addDependency(node);
                }
                return new OnHookEntry(b, method, mh, v);
            });
        }
    }

    static OnHookContainerNode newNode(ClassProcessor cpr, Class<? extends Hook> hookType, UncheckedThrowableFactory<? extends RuntimeException> tf) {
        Class<?> cl = ClassFinder.findDeclaredClass(hookType, "Builder", Hook.Builder.class);
        ClassProcessor cp = cpr.spawn(cl);
        MethodHandle constructor = ConstructorFinder.find(cp, tf);
        // TODO validate type variable
        return new OnHookContainerNode(hookType, cp, constructor);
    }

    public void process() {
        root.cp.findMethods(m -> onMethod(root, m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY));
        for (OnHookContainerNode b = unprocessedNodes.pollFirst(); b != null; b = unprocessedNodes.pollFirst()) {
            OnHookContainerNode bb = b;
            b.cp.findMethods(m -> onMethod(bb, m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY));
        }

        boolean isEmpty = onHookAnnotatedFields == null && onHookAnnotatedMethods == null && onHookAnnotatedTypes == null && onHookAssignableTos == null
                && onHookCustomHooks == null;

        // It's okay, for example, for bundle to not have any roots.
        if (isEmpty && Hook.Builder.class.isAssignableFrom(root.cp.clazz())) {
            throw new AssertionError("There must be at least one method annotated with @OnHook on " + root.cp.clazz());
        }

        int index = 0;
        sorted.add(root);

        // Uses a simple iterative algorithm, that keeps on going as long as progress is made.
        // It is potentially O(n^2) but this should not be a problem in practice
        boolean doContinue = true;
        while (doContinue && !nodes.isEmpty()) {
            doContinue = false;
            for (Iterator<OnHookContainerNode> iterator = nodes.values().iterator(); iterator.hasNext();) {
                OnHookContainerNode b = iterator.next();
                if (!b.hasUnresolvedDependencies()) {
                    b.id = ++index;
                    sorted.add(b);
                    iterator.remove();
                    doContinue = true;
                }
            }
        }

        if (!nodes.isEmpty()) {
            // Okay, we got some circles.
            throw new UnsupportedOperationException("Not supported currently");
        }

        // We do really simple

        // Top search
        // int prevSize = builders.size();
    }

    @SuppressWarnings("unchecked")
    private void process(OnHookContainerNode b, Parameter p, Method method, MethodHandle mh, IdentityHashMap<?, OnHookEntry> map) {
        ParameterizedType pt = (ParameterizedType) p.getParameterizedType();
        Class<?> typeVariable = (Class<?>) pt.getActualTypeArguments()[0];
        ((IdentityHashMap<Class<?>, OnHookEntry>) map).compute(typeVariable, (k, v) -> new OnHookEntry(b, method, mh, v));
    }

    static final class OnHookContainerNode {

        /** The class processor used for iterating over methods. */
        final ClassProcessor cp;

        @Nullable
        Set<OnHookContainerNode> dependencies;

        /** The i */
        int id;

        @Nullable
        final MethodHandle constructor;

        final Class<? extends Hook> hookType;

        OnHookContainerNode(Class<? extends Hook> hookType, ClassProcessor cp, MethodHandle constructor) {
            this.hookType = requireNonNull(hookType);
            this.cp = requireNonNull(cp);
            this.constructor = constructor;
            if (constructor != null && constructor.type().returnType() != cp.clazz()) {
                throw new IllegalStateException("OOPS");
            }
        }

        void addDependency(OnHookContainerNode b) {
            Set<OnHookContainerNode> d = dependencies;
            if (d == null) {
                d = dependencies = new HashSet<>();
            }
            d.add(b);
        }

        boolean hasUnresolvedDependencies() {
            if (dependencies != null) {
                for (OnHookContainerNode ch : dependencies) {
                    if (ch.id == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return constructor == null ? "" : constructor.type().toString();
        }
    }

    static final class OnHookEntry {
        final OnHookContainerNode builder;
        final Method method;
        final MethodHandle methodHandle;

        @Nullable
        final OnHookEntry next;

        OnHookEntry(OnHookContainerNode builder, Method method, MethodHandle methodHandle, OnHookEntry next) {
            this.builder = requireNonNull(builder);
            this.method = requireNonNull(method);
            this.methodHandle = requireNonNull(methodHandle);
            this.next = next;
        }
    }

}
// if rootClass.getModule() != hook.class.getModule and hook.class.getModule() is not open...
/// Complain.

// STEP 1
/// Find All methods
/// Validate Parameters
/// Go into dependencies...

// Find them, validate parameters
// Validate we can make MethodHandle

// Step 2
// Validate no