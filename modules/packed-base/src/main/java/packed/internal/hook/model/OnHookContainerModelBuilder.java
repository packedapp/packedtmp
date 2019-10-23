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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayDeque;
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
public final class OnHookContainerModelBuilder {

    private static final UncheckedThrowableFactory<? extends RuntimeException> tf = UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY;

    final MutableOnHookMap<LinkedEntry> allEntries = new MutableOnHookMap<>();

    final MutableOnHookMap<LinkedEntry> rootEntries;

    /** All non-root nodes. */
    private final IdentityHashMap<Class<? extends Hook>, Node> nodes = new IdentityHashMap<>();

    /** The root node. */
    private final Node root;

    final ArrayDeque<Node> stack = new ArrayDeque<>();

    public OnHookContainerModelBuilder(ClassProcessor cp, Class<?>... additionalParameters) {
        if (Hook.class.isAssignableFrom(cp.clazz())) {
            this.root = new Node(cp, cp.clazz());
            rootEntries = allEntries;
        } else {
            this.root = new Node(cp);
            rootEntries = new MutableOnHookMap<>();

        }
    }

    public OnHookContainerModel build() {
        // Find all methods annotated with @OnHook and process them.
        root.cp.findMethods(m -> onMethod(root, m));
        for (Node b = stack.pollFirst(); b != null; b = stack.pollFirst()) {
            Node bb = b;
            bb.cp.findMethods(m -> onMethod(bb, m));
        }

        // Roots are only required to have OnHook if they are an Hook themself.
        // For example, Extension and Bundle should not fail here.
        if (allEntries.isEmpty() && Hook.Builder.class.isAssignableFrom(root.cp.clazz())) {
            throw new AssertionError("There must be at least one method annotated with @OnHook on " + root.cp.clazz());
        }

        // Uses a simple iterative algorithm, to make sure there are no interdependencies between the custom hooks
        // It is potentially O(n^2) but this should not be a problem in practice
        // We add each no with no dependencies to the end of the stack.
        int index = nodes.size();
        boolean doContinue = true;
        while (doContinue && !nodes.isEmpty()) {
            doContinue = false;
            for (Iterator<Node> iterator = nodes.values().iterator(); iterator.hasNext();) {
                Node b = iterator.next();
                if (!b.hasUnresolvedDependencies()) {
                    b.index = index--;
                    stack.addFirst(b);
                    iterator.remove();
                    doContinue = true;
                }
            }
        }
        stack.addFirst(root);

        if (!nodes.isEmpty()) {
            // Okay, we got some circles.
            throw new UnsupportedOperationException("Not supported currently");
        }
        return new OnHookContainerModel(this);
    }

    private void onMethod(Node node, Method method) {
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
        if (!Hook.class.isAssignableFrom(hook.getType())) {
            throw tf.newThrowableForMethod("The first parameter of a method annotated with @" + OnHook.class.getSimpleName() + " must be of type "
                    + Hook.class.getCanonicalName() + " was " + parameters[0].getType(), method);
        }
        @SuppressWarnings("unchecked")
        Class<? extends Hook> hookType = (Class<? extends Hook>) hook.getType();

        for (int i = 1; i < parameters.length; i++) {
            if (node != root) {
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

        MethodHandle mh = node.cp.unreflect(method, tf);

        // Let first see if it is a base book.
        final IdentityHashMap<Class<?>, LinkedEntry> mm;

        // Keep track of all dangling stuff on root
        final IdentityHashMap<Class<?>, LinkedEntry> roots;
        if (hookType == AnnotatedFieldHook.class) {
            mm = allEntries.annotatedFieldsLazyInit();
            roots = node == root && root.builderConstructor == null ? rootEntries.annotatedFieldsLazyInit() : null;
        } else if (hookType == AnnotatedMethodHook.class) {
            mm = allEntries.annotatedMethodsLazyInit();
            roots = node == root && root.builderConstructor == null ? rootEntries.annotatedMethodsLazyInit() : null;
        } else if (hookType == AnnotatedTypeHook.class) {
            mm = allEntries.annotatedTypesLazyInit();
            roots = node == root && root.builderConstructor == null ? rootEntries.annotatedTypesLazyInit() : null;
        } else if (hookType == AssignableToHook.class) {
            mm = allEntries.assignableTosLazyInit();
            roots = node == root && root.builderConstructor == null ? rootEntries.assignableTos : null;
        } else {
            mm = null;
            roots = null;
        }

        if (mm != null) {
            ParameterizedType pt = (ParameterizedType) hook.getParameterizedType();
            Class<?> typeVariable = (Class<?>) pt.getActualTypeArguments()[0];
            mm.compute(typeVariable, (k, v) -> new LinkedEntry(node, mh, v));
            if (roots != null) {
                // roots.compute(typeVariable, (k, v) -> new LinkedEntry(node, mh, v));
            }
        } else {
            if (hookType == node.cp.clazz()) {
                tf.newThrowableForMethod("Hook cannot depend on itself", method);
            }
            TypeUtil.checkClassIsInstantiable(hookType);
            IdentityHashMap<Class<?>, LinkedEntry> m = allEntries.customHooksLazyInit();
            m.compute(hookType, (k, v) -> {

                // Lazy create new node if one does not already exist for the hookType
                Node nodeRef = nodes.computeIfAbsent(hookType, ignore -> {
                    Node newNode = new Node(root.cp, hookType);
                    stack.addLast(newNode); // make sure it will be processed at some later point.
                    return newNode;
                });

                // Test if the builder of a hooks depends on the hook itself
                if (node == nodeRef) {
                    throw tf.newThrowableForMethod("Hook cannot depend on itself", method);
                }

                // Or maybe we need to this for circles??
                // If we have pure tests
                if (node != root) {
                    node.addDependency(nodeRef);
                }
                return new LinkedEntry(node, mh, v);
            });
        }
    }

    static final class LinkedEntry {

        final Node node;

        final MethodHandle methodHandle;

        @Nullable
        final LinkedEntry next;

        LinkedEntry(Node builder, MethodHandle methodHandle, LinkedEntry next) {
            this.node = requireNonNull(builder);
            this.methodHandle = requireNonNull(methodHandle);
            this.next = next;
        }
    }

    static final class Node {

        /** A constructor for the builder if this node is a custom hook. */
        @Nullable
        final MethodHandle builderConstructor;

        /** The class processor for the entity that contains the methods annotated with {@link OnHook}. */
        private final ClassProcessor cp;

        /** Any dependencies on other nodes. */
        @Nullable
        private Set<Node> dependencies;

        /** The index of this node. */
        int index;

        /** The type of on node container. */
        final Class<?> onNodeContainerType;

        /**
         * A node without a builder
         * 
         * @param cp
         *            the class processor for the node
         */
        Node(ClassProcessor cp) {
            this.cp = cp;
            this.onNodeContainerType = cp.clazz();
            this.builderConstructor = null;
        }

        Node(ClassProcessor cps, Class<?> type) {
            this.onNodeContainerType = requireNonNull(type);

            Class<?> builderClass = ClassFinder.findDeclaredClass(type, "Builder", Hook.Builder.class);
            this.cp = cps.spawn(builderClass);

            this.builderConstructor = ConstructorFinder.find(cp, tf);
            if (builderConstructor.type().returnType() != cp.clazz()) {
                throw new IllegalStateException("OOPS");
            }
        }

        void addDependency(Node b) {
            Set<Node> d = dependencies;
            if (d == null) {
                d = dependencies = new HashSet<>();
            }
            d.add(b);
        }

        boolean hasUnresolvedDependencies() {
            if (dependencies != null) {
                for (Node ch : dependencies) {
                    if (ch.index == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return builderConstructor == null ? "" : builderConstructor.type().toString();
        }
    }
}
