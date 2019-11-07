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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.hook.OnHookModelBuilder.Node;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.TinyPair;
import packed.internal.util.UncheckedThrowableFactory;

/** A model of a container with {@link OnHook} methods. */
public final class OnHookModel {

    static final boolean DEBUG = true;

    final ImmutableOnHookMap<Link> allLinks;

    /** Constructors for each builder. */
    final MethodHandle[] builderConstructors;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    final Link[] customHooks;

    OnHookModel(OnHookModelBuilder b) {
        Function<TinyPair<Node, MethodHandle>, Link> ff = e -> {
            Link l = null;
            for (; e != null; e = e.next) {
                l = new Link(e.element2, e.element1.index, l);
            }
            return l;
        };

        allLinks = b.allEntries.toImmutable(ff);

        this.customHooks = new Link[b.stack.size()];
        this.builderConstructors = new MethodHandle[b.stack.size()];
        List<OnHookModelBuilder.Node> list = List.copyOf(b.stack);
        if (DEBUG) {
            for (int i = 0; i < list.size(); i++) {
                OnHookModelBuilder.Node n = list.get(i);
                String msg = i + " " + n.index + " " + n.containerType;
                if (n.builderConstructor != null) {
                    msg += " " + n.builderConstructor.type().returnType();
                }
                System.out.println(msg);
            }
            System.out.println("------");

            System.out.println("An Methods " + allLinks.annotatedMethods);

            System.out.println("An Fields " + allLinks.annotatedFields);
            System.out.println("An Types " + allLinks.annotatedTypes);
            System.out.println("Types " + allLinks.assignableTos);
            System.out.println("------");
            // System.out.println("Methods " + allLinks.annotatedMethods);

        }
        for (int i = 0; i < list.size(); i++) {
            OnHookModelBuilder.Node n = list.get(i);// b.result.get(i);
            builderConstructors[i] = n.builderConstructor;
            if (b.allEntries.customHooks != null) {
                // We reverse the order here so instead of Dependent->Dependency we get Dependency->Dependent
                // We do this so we do not automatically invoke methods on the root object. which is never cached.
                for (TinyPair<Node, MethodHandle> l = b.allEntries.customHooks.get(n.containerType); l != null; l = l.next) {
                    customHooks[l.element1.index] = new Link(l.element2, i, customHooks[l.element1.index]);
                }
            }
        }
    }

    /**
     * Returns an immutable set of all annotations on fields that are we are hooked on.
     * 
     * @return the set
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Set<Class<? extends Annotation>> annotatedFieldHooks() {
        return allLinks == null || allLinks.annotatedFields == null ? null : (Set) allLinks.annotatedFields.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Set<Class<? extends Annotation>> annotatedMethodHooks() {
        return allLinks == null || allLinks.annotatedMethods == null ? null : (Set) allLinks.annotatedMethods.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Set<Class<? extends Annotation>> annotatedTypeHooks() {
        return allLinks == null || allLinks.annotatedTypes == null ? null : (Set) allLinks.annotatedTypes.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Set<Class<?>> assignableTos() {
        return allLinks == null || allLinks.assignableTos == null ? null : (Set) allLinks.assignableTos.keySet();
    }

    /**
     * Creates a new model.
     * 
     * @param cp
     *            the class processor
     * @param additionalParameters
     *            any additional parameter types allowed.
     * @return the new model, or null if the no {@link OnHook} annotations was present
     */
    @Nullable
    public static OnHookModel newInstance(ClassProcessor cp, boolean instantiateRoot, UncheckedThrowableFactory<? extends RuntimeException> tf,
            Class<?>... additionalParameters) {
        return new OnHookModelBuilder(cp, instantiateRoot, tf, additionalParameters).build();
    }

    static class Link {
        final int index;
        final MethodHandle mh;
        @Nullable
        final Link next;

        private Link(MethodHandle mh, int index, @Nullable Link next) {
            this.mh = requireNonNull(mh);
            this.index = index;
            this.next = next;
        }
    }

    // Lad os lige taenke over om vi skal bruge det andet steds...
    static final class ImmutableOnHookMap<V> {

        /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
        @Nullable
        final Map<Class<?>, V> annotatedFields;

        /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
        @Nullable
        final Map<Class<?>, V> annotatedMethods;

        /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
        @Nullable
        final Map<Class<?>, V> annotatedTypes;

        /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
        @Nullable
        final Map<Class<?>, V> assignableTos;

        ImmutableOnHookMap(Map<Class<?>, V> annotatedFields, Map<Class<?>, V> annotatedMethod, Map<Class<?>, V> annotatedTypes,
                Map<Class<?>, V> assignableTos) {
            this.annotatedFields = annotatedFields;
            this.annotatedMethods = annotatedMethod;
            this.annotatedTypes = annotatedTypes;
            this.assignableTos = assignableTos;
        }

        @Override
        public String toString() {
            return "AnnotatedFields: " + toString(annotatedFields) + ", " + "annotatedMethods: " + toString(annotatedMethods) + ", " + "annotatedTypes: "
                    + toString(annotatedTypes) + ", " + "assignableTos: " + toString(assignableTos) + ", ";
        }

        private String toString(Map<?, ?> m) {
            return m == null ? "{}" : m.keySet().toString();
        }
    }
}
