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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import app.packed.base.Nullable;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hook.OnHookModelBuilder.Node;
import packed.internal.reflect.OpenClass;
import packed.internal.util.TinyPair;

/** A model of a container with {@link OnHook} methods. */
public final class OnHookModel {

    static final boolean DEBUG = false;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedFieldHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> annotatedFields;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedMethodHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> annotatedMethods;

    /** Methods annotated with {@link OnHook} that takes a {@link AnnotatedTypeHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> annotatedTypes;

    /** Methods annotated with {@link OnHook} that takes a {@link AssignableToHook} as a parameter. */
    @Nullable
    final Map<Class<?>, Link> assignableTos;

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
        annotatedFields = toImmutable0(b.annotatedFields, ff);
        annotatedMethods = toImmutable0(b.annotatedMethods, ff);
        annotatedTypes = toImmutable0(b.annotatedTypes, ff);
        assignableTos = toImmutable0(b.assignableTos, ff);

        this.customHooks = new Link[b.stack.size()];
        this.builderConstructors = new MethodHandle[b.stack.size()];
        List<OnHookModelBuilder.Node> list = List.copyOf(b.stack);
        if (DEBUG) {
            for (int i = 0; i < list.size(); i++) {
                OnHookModelBuilder.Node n = list.get(i);
                String msg = i + " " + n.index + " " + n.hookType;
                if (n.builderConstructor != null) {
                    msg += " " + n.builderConstructor.type().returnType();
                }
                System.out.println(msg);
            }
            System.out.println("------");

            System.out.println("An Methods " + annotatedMethods);

            System.out.println("An Fields " + annotatedFields);
            System.out.println("An Types " + annotatedTypes);
            System.out.println("Types " + assignableTos);
            System.out.println("------");
            // System.out.println("Methods " + allLinks.annotatedMethods);

        }
        for (int i = 0; i < list.size(); i++) {
            OnHookModelBuilder.Node n = list.get(i);// b.result.get(i);
            builderConstructors[i] = n.builderConstructor;
            if (b.customHooks != null) {
                // We reverse the order here so instead of Dependent->Dependency we get Dependency->Dependent
                // We do this so we do not automatically invoke methods on the root object. which is never cached.
                for (TinyPair<Node, MethodHandle> l = b.customHooks.get(n.hookType); l != null; l = l.next) {
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
        return annotatedFields == null ? null : (Set) annotatedFields.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Set<Class<? extends Annotation>> annotatedMethodHooks() {
        return annotatedMethods == null ? null : (Set) annotatedMethods.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Set<Class<? extends Annotation>> annotatedTypeHooks() {
        return annotatedTypes == null ? null : (Set) annotatedTypes.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Nullable
    public Set<Class<?>> assignableTos() {
        return assignableTos == null ? null : (Set) assignableTos.keySet();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<Class<?>, Link> toImmutable0(IdentityHashMap<Class<?>, TinyPair<Node, MethodHandle>> map, Function<TinyPair<Node, MethodHandle>, Link> f) {
        if (map == null) {
            return null;
        }
        // Replace in map
        IdentityHashMap m = map;

        m.replaceAll((k, v) -> ((Function) f).apply(v));

        return Map.copyOf(m);
    }

    @Override
    public String toString() {
        return "AnnotatedFields: " + toString(annotatedFields) + ", " + "annotatedMethods: " + toString(annotatedMethods) + ", " + "annotatedTypes: "
                + toString(annotatedTypes) + ", " + "assignableTos: " + toString(assignableTos) + ", ";
    }

    private String toString(Map<?, ?> m) {
        return m == null ? "{}" : m.keySet().toString();
    }

    /**
     * Creates a new model.
     * 
     * @param cp
     *            the class processor
     * @return the new model, or null if the no {@link OnHook} annotations was present
     */
    @Nullable
    public static OnHookModel newModel(OpenClass cp, boolean instantiateRoot, UncheckedThrowableFactory<? extends RuntimeException> tf) {
        return new OnHookModelBuilder(cp, instantiateRoot, tf).build();
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
}
