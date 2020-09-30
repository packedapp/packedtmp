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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.InternalExtensionException;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import packed.internal.classscan.invoke.OpenClass;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.thirdparty.guice.GTypeLiteral;
import packed.internal.util.AnnotationUtil;
import packed.internal.util.StringFormatter;
import packed.internal.util.Tiny;
import packed.internal.util.TinyPair;
import packed.internal.util.TypeUtil;

/** A builder for classes that may contain methods annotated with {@link OnHook}. */
final class OnHookModelBuilder {

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> annotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> annotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> assignableTos;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> customHooks;

    /** All non-root nodes, the key being the type of the hook. */
    private IdentityHashMap<Class<? extends Hook>, HookBuilderNode> nodes;

    /** The root node (is never in {@link #nodes}). */
    private final Node root;

    /** A stack used for processing node. */
    final ArrayDeque<Node> stack = new ArrayDeque<>();

    private final UncheckedThrowableFactory<? extends RuntimeException> tf;

    OnHookModelBuilder(OpenClass cp, boolean instantiateRoot, UncheckedThrowableFactory<? extends RuntimeException> tf) {
        this.root = instantiateRoot ? new HookBuilderNode(cp, tf, cp.type()) : new Node(cp);
        this.tf = requireNonNull(tf);
    }

    @Nullable
    OnHookModel build() {
        // Find all methods annotated with @OnHook and process them.
        // If we support it on components, we need a little change..
        root.cp.findMethods(m -> onMethod(root, m));
        for (Node b = stack.pollFirst(); b != null; b = stack.pollFirst()) {
            Node bb = b;
            bb.cp.findMethods(m -> onMethod(bb, m));
        }

        if (annotatedFields == null && annotatedMethods == null && assignableTos == null && customHooks == null) {
            return null;
        }

        // Uses a simple iterative algorithm, to make sure there are no interdependencies between the custom hooks
        // It is potentially O(n^2) but this should not be a problem in practice
        // We add each no with no dependencies to the end of the stack.
        if (nodes != null) {
            int index = nodes.size();
            boolean doContinue = true;
            while (doContinue && !nodes.isEmpty()) {
                doContinue = false;
                for (Iterator<HookBuilderNode> iterator = nodes.values().iterator(); iterator.hasNext();) {
                    HookBuilderNode b = iterator.next();
                    if (!Tiny.anyMatch(b.dependencies, e -> e.index == 0)) {
                        b.index = index--;
                        stack.addFirst(b);
                        iterator.remove();
                        doContinue = true;
                    }
                }
            }
            // Check if there are any remaining circles -> Not a DAG
            if (!nodes.isEmpty()) {
                throw new UnsupportedOperationException("Not supported currently");
            }
        }
        stack.addFirst(root);

        return new OnHookModel(this);
    }

    private Type getResolvedType(Class<?> c, Method p, Type t) {
        if (TypeUtil.isFreeFromTypeVariables(t)) {

            return t;
        }

        Type t2 = GTypeLiteral.get(c).resolveType(t);
        // System.out.println(t2);
        if (TypeUtil.isFreeFromTypeVariables(t2)) {
            return t2;
        }
        // Still unresolved type parameters
        throw new Error();
    }

    @SuppressWarnings("unchecked")
    private void onMethod(Node node, Method method) {
        // Ignore any method that is not annotated with @OnHook
        if (!method.isAnnotationPresent(OnHook.class)) {
            return;
        }

        if (method.getParameterCount() == 0) {
            throw tf.newThrowableForMethod(
                    "Methods annotated with @" + OnHook.class.getSimpleName() + " must take at least 1 parameter of type " + Hook.class.getCanonicalName(),
                    method);
        }

        Parameter[] parameters = method.getParameters();
        Parameter hook = parameters[0];

        Type hookT = getResolvedType(node.cp.type(), method, hook.getParameterizedType());

        Class<?> rawHookType = GTypeLiteral.get(hookT).getRawType();

        @SuppressWarnings("rawtypes")
        Class<? extends Hook> hookType = (Class) GTypeLiteral.get(hookT).getRawType();

        if (!Hook.class.isAssignableFrom(rawHookType)) {
            throw tf.newThrowableForMethod("The first parameter of a method annotated with @" + OnHook.class.getSimpleName() + " must be of type "
                    + Hook.class.getCanonicalName() + " was " + parameters[0].getType(), method);
        }

        // Validate remaining parameters
        for (int i = 1; i < parameters.length; i++) {
            if (node instanceof HookBuilderNode) {
                throw tf.newThrowableForMethod(
                        "Implementations of Hook.Builder can only take a single parameter for methods annotated with @" + OnHook.class.getSimpleName(), method);
            }
            if (Hook.class.isAssignableFrom(parameters[i].getType())) {
                throw tf.newThrowableForMethod("Cannot have more than 1 parameter that are instances of " + Hook.class.getCanonicalName(), method);
            }
            //
            // // If we have additional parameters on our initial builder, check that they are okay.
            // if (b == root && parameters.length > 1) {
            // // Check that the remaining are okay
            // // Probably want these additional parameters in a list to Entry
            // }
        }

        // Process the hook either as a base hook (Annotated*Hook+InstanceOfHook) or a custom hook (anything else implementing
        // Hook)
        if (hookType == AnnotatedFieldHook.class) {
            IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> mm = annotatedFields;
            if (mm == null) {
                mm = annotatedFields = new IdentityHashMap<>(1);
            }
            onMethodBaseHook(node, hookT, hookType, method, mm);
        } else if (hookType == AnnotatedMethodHook.class) {
            IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> mm = annotatedMethods;
            if (mm == null) {
                mm = annotatedMethods = new IdentityHashMap<>(1);
            }
            onMethodBaseHook(node, hookT, hookType, method, mm);
        }
        /*
         * else if (hookType == AnnotatedTypeHook.class) { IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> mm =
         * annotatedTypes; if (mm == null) { mm = annotatedTypes = new IdentityHashMap<>(1); } onMethodBaseHook(node, hookT,
         * hookType, method, mm); }
         */
        else if (hookType == AssignableToHook.class) {
            IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> mm = assignableTos;
            if (mm == null) {
                mm = assignableTos = new IdentityHashMap<>(1);
            }
            onMethodBaseHook(node, hookT, hookType, method, mm);
        } else {
            onMethodCustomHook(node, hookType, method);
        }
    }

    @SuppressWarnings("unchecked")
    void onMethodBaseHook(Node node, Type t, Class<? extends Hook> hookType, Method method, IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> mm) {
        MethodHandle mh = node.cp.unreflect(method, tf);

        // https://github.com/google/gson/blob/master/gson/src/main/java/com/google/gson/internal/%24Gson%24Types.java
        if (!(t instanceof ParameterizedType)) {
            throw tf.newThrowableForMethod(hookType.getSimpleName() + " must be parameterized, cannot be a raw type", method);
        }
        ParameterizedType pt = (ParameterizedType) t;
        Type type = pt.getActualTypeArguments()[0];
        if (!(type instanceof Class)) {
            throw tf.newThrowable("Only class qualified supported, was " + pt);
        }
        Class<?> qualifierType = (Class<?>) type;

        if (hookType != AssignableToHook.class && !AnnotationUtil.hasRuntimeRetentionPolicy((Class<? extends Annotation>) qualifierType)) {
            throw tf.newThrowable(hookType + " must be qualified with an annotation that has runtime retention policy");
        }

        mm.compute(qualifierType, (k, v) -> new TinyPair<>(node, mh, v));
    }

    void onMethodCustomHook(Node node, Class<? extends Hook> hookType, Method method) {
        if (hookType == node.cp.type()) {
            throw tf.newThrowableForMethod("Hook cannot depend on itself", method);
        }

        MethodHandle mh = node.cp.unreflect(method, tf);
        IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> m = customHooks;
        if (m == null) {
            m = customHooks = new IdentityHashMap<>(1);
        }

        m.compute(hookType, (k, v) -> {

            // Lazy create new node if one does not already exist for the hookType
            if (nodes == null) {
                nodes = new IdentityHashMap<>();
            }
            HookBuilderNode customHookRef = nodes.computeIfAbsent(hookType, ignore -> {
                HookBuilderNode newNode = new HookBuilderNode(root.cp, tf, hookType);
                stack.addLast(newNode); // make sure it will be processed at some later point.
                return newNode;
            });

            // Test if the builder of a hooks depends on the hook itself
            if (node == customHookRef) {
                throw tf.newThrowableForMethod("Hook cannot depend on itself", method);
            }

            // This looks wrong, shouldnt it be a dependency on customHookRef????
            if (node instanceof HookBuilderNode) {
                HookBuilderNode bn = (HookBuilderNode) node;
                bn.dependencies = new Tiny<>(bn, bn.dependencies);
            }
            return new TinyPair<>(node, mh, v);
        });
    }

    static class HookBuilderNode extends Node {

        /** Dependencies on other nodes (will never contain a link to the root node). */
        @Nullable
        Tiny<HookBuilderNode> dependencies;

        private HookBuilderNode(OpenClass cps, UncheckedThrowableFactory<? extends RuntimeException> tf, Class<?> type) {
            super(cps, tf, type);
        }
    }

    /** A node represents a "container" class with one or more methods annotated with {@link OnHook}. */
    static class Node {

        /** A constructor for the builder if this node is a custom hook. */
        @Nullable
        final MethodHandle builderConstructor;

        /** The class processor for the entity that contains the methods annotated with {@link OnHook}. */
        private final OpenClass cp;

        /** The type of hook for non-root nodes. */
        @Nullable
        final Class<?> hookType;

        /** The index of this node. */
        int index;

        /**
         * Creates a node for a container that does not have a builder defined. This is, for example, the case for both
         * {@link Bundle} and {@link Extension} which is instantiated elsewhere then the hook subsystem.
         * 
         * @param cp
         *            the class processor for the node
         */
        private Node(OpenClass cp) {
            this.cp = requireNonNull(cp);
            this.hookType = null;
            this.builderConstructor = null;
        }

        private Node(OpenClass cps, UncheckedThrowableFactory<? extends RuntimeException> tf, Class<?> type) {
            this.hookType = requireNonNull(type);
            Class<?> builderClass = findDeclaredClass(type, "Builder", Hook.Builder.class);
            this.cp = cps.spawn(builderClass);
            this.builderConstructor = find(cp, tf);

            // TypeUtil.checkClassIsInstantiable(hookType);

            if (builderConstructor.type().returnType() != cp.type()) {
                throw new IllegalStateException("OOPS");
            }
        }

        // Could have name = type.getSipleName?
        // TODO include subclasses if they are assignable to type
        public static Class<?> findDeclaredClass(Class<?> declaringClass, String name, Class<?> type) {
            Class<?> groupType = null;

            for (Class<?> c : declaringClass.getDeclaredClasses()) {
                if (c.getSimpleName().equals(name)) {
                    if (!type.isAssignableFrom(c)) {
                        throw new InternalExtensionException(c.getCanonicalName() + " must extend " + StringFormatter.format(type));
                    }
                    groupType = c;
                }
            }

            if (groupType == null) {
                for (Class<?> c : declaringClass.getSuperclass().getDeclaredClasses()) {
                    if (c.getSimpleName().equals(name)) {
                        if (!type.isAssignableFrom(c)) {
                            throw new InternalExtensionException(c.getCanonicalName() + " must extend " + StringFormatter.format(type));
                        }
                        groupType = c;
                    }
                }
            }

            if (groupType == null) {
                throw new IllegalArgumentException("Could not find declared class named " + name + " for " + declaringClass);
            }

            return groupType;
        }

        /**
         * Finds a constructor (method handle).
         * 
         * @param cp
         *            the type to find the constructor or
         * @param parameterTypes
         *            the parameter types the constructor must take
         * @return a method handle
         */
        static <T extends RuntimeException> MethodHandle find(OpenClass cp, UncheckedThrowableFactory<T> tf, Class<?>... parameterTypes) throws T {
            Class<?> onType = cp.type();
            if (Modifier.isAbstract(onType.getModifiers())) {
                throw tf.newThrowable("'" + StringFormatter.format(onType) + "' cannot be an abstract class");
            } else if (TypeUtil.isInnerOrLocalClass(onType)) {
                throw tf.newThrowable("'" + StringFormatter.format(onType) + "' cannot be an inner or local class");
            }

            // First check that we have a constructor with specified parameters.
            // We could use Lookup.findSpecial, but we need to register the constructor if we are generating a native image.
            Constructor<?> constructor = null;
            try {
                constructor = onType.getDeclaredConstructor(parameterTypes);
            } catch (NoSuchMethodException e) {
                if (Extension.class.isAssignableFrom(onType)) {
                    // Hack
                    try {
                        constructor = onType.getDeclaredConstructor(ExtensionConfiguration.class);
                    } catch (NoSuchMethodException ignore) {} // Already on failure path
                }
                if (constructor == null) {
                    if (parameterTypes.length == 0) {
                        throw tf.newThrowable("'" + StringFormatter.format(onType) + "' must have a no-argument constructor");
                    } else {
                        throw tf.newThrowable("'" + StringFormatter.format(onType) + "' must have a constructor taking ["
                                + Stream.of(parameterTypes).map(p -> p.getName()).collect(Collectors.joining(",")) + "]");
                    }
                }
            }

            return cp.unreflectConstructor(constructor, tf);
        }

        @Override
        public String toString() {
            return builderConstructor == null ? "" : builderConstructor.type().toString();
        }
    }
}
