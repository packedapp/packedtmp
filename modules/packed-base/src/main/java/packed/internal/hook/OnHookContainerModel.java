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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import app.packed.hook.Hook.Builder;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.hook.HookRequest.DelayedAnnotatedField;
import packed.internal.hook.HookRequest.DelayedAnnotatedMethod;
import packed.internal.hook.OnHookContainerModelBuilder.Node;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.UncheckedThrowableFactory;
import packed.internal.util.tiny.TinyPairNode;

/**
 *
 */
public final class OnHookContainerModel {

    static final boolean DEBUG = false;
    private final ImmutableOnHookMap<Link> allLinks;

    /** Constructors for each builder. */
    private final MethodHandle[] builderConstructors;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    private final Link[] customHooks;

    final ImmutableOnHookMap<Link> rootLinks;

    final boolean isHookTop;

    OnHookContainerModel(OnHookContainerModelBuilder b) {
        Function<TinyPairNode<Node, MethodHandle>, Link> ff = e -> {
            Link l = null;
            for (; e != null; e = e.next) {
                l = new Link(e.element2, e.element1.index, l);
            }
            return l;
        };

        allLinks = b.allEntries.toImmutable(ff);
        if (b.allEntries != b.rootEntries) {
            rootLinks = b.rootEntries.toImmutable(ff);
        } else {
            rootLinks = allLinks;
        }

        this.customHooks = new Link[b.stack.size()];
        this.builderConstructors = new MethodHandle[b.stack.size()];
        List<OnHookContainerModelBuilder.Node> list = List.copyOf(b.stack);
        if (DEBUG) {
            for (int i = 0; i < list.size(); i++) {
                OnHookContainerModelBuilder.Node n = list.get(i);
                String msg = i + " " + n.index + " " + n.onNodeContainerType;
                if (n.builderConstructor != null) {
                    msg += " " + n.builderConstructor.type().returnType();
                }
                System.out.println(msg);
            }
            if (rootLinks != null) {
                System.out.println(rootLinks.toString());
            } else {
                System.out.println("No rootlinks");
            }
            System.out.println("------");
        }
        for (int i = 0; i < list.size(); i++) {
            OnHookContainerModelBuilder.Node n = list.get(i);// b.result.get(i);
            builderConstructors[i] = n.builderConstructor;
            if (b.allEntries.customHooks != null) {
                // We reverse the order here so instead of Dependent->Dependency we get Dependency->Dependent
                // We do this so we do not automatically invoke methods on the root object. which is never cached.
                for (TinyPairNode<Node, MethodHandle> l = b.allEntries.customHooks.get(n.onNodeContainerType); l != null; l = l.next) {
                    customHooks[l.element1.index] = new Link(l.element2, i, customHooks[l.element1.index]);
                }
            }
        }
        isHookTop = b.isTopHook;
    }

    /**
     * Returns an immutable set of all field triggering annotations types.
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

    private Hook.Builder<?> builderOf(Object[] array, int index) throws Throwable {
        Object builder = array[index];
        if (builder == null) {
            builder = array[index] = builderConstructors[index].invoke();
        }
        return (Builder<?>) builder;
    }

    CachedHook<Hook> compute(Object[] array) throws Throwable {
        // This code is same as process()
        for (int i = array.length - 1; i >= 0; i--) {
            for (Link link = customHooks[i]; link != null; link = link.next) {
                if (builderConstructors[i] != null) {
                    Hook.Builder<?> builder = builderOf(array, i);
                    link.mh.invoke(builder, array[link.index]);
                }
            }
            if (i > 0) {
                Object h = array[i];
                if (h != null) {
                    array[i] = ((Hook.Builder<?>) h).build();
                }
            }
        }

        CachedHook<Hook> result = null;
        for (Link link = customHooks[0]; link != null; link = link.next) {
            result = new CachedHook<>(link.mh, (Hook) array[link.index], result);
        }
        return result;
    }

    @Nullable
    public Object process(@Nullable Object parent, ClassProcessor cpTarget, UncheckedThrowableFactory<?> tf) throws Throwable {
        HookProcessor hc = new HookProcessor(cpTarget, tf);
        HookRequest.Builder hb = new HookRequest.Builder(this, hc);
        Object[] array = hb.array;
        array[0] = parent;
        cpTarget.findMethodsAndFields(allLinks.annotatedMethods == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                tryProcesAnnotatedMethod(hc, f, a, hb);
            }
        }, allLinks.annotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                tryProcesAnnotatedField(hc, f, a, hb);
            }
        });
        hc.close();

        compute(array);
        if (parent != null) {
            return parent;
        }
        Object a = array[0];
        return a == null ? null : ((Hook.Builder<?>) a).build();
    }

    int size() {
        return builderConstructors.length;
    }

    public void tryProcesAnnotatedField(HookProcessor hc, Field field, Annotation annotation, HookRequest.Builder hr) throws Throwable {
        for (Link link = allLinks.annotatedFields.get(annotation.annotationType()); link != null; link = link.next) {
            if (link.index == 0 && !isHookTop) {
                hr.delayedFields.add(new DelayedAnnotatedField(hc.cp, field, annotation, link.mh));
            } else {
                Hook.Builder<?> builder = builderOf(hr.array, link.index);
                AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(hc, field, annotation);
                if (link.mh.type().parameterCount() == 1) {
                    link.mh.invoke(hook);
                } else {
                    link.mh.invoke(builder, hook);
                }
            }
        }
    }

    public void tryProcesAnnotatedMethod(HookProcessor hc, Method method, Annotation annotation, HookRequest.Builder hr) throws Throwable {
        for (Link link = allLinks.annotatedMethods.get(annotation.annotationType()); link != null; link = link.next) {
            if (link.index == 0 && !isHookTop) {
                hr.delayedMethods.add(new DelayedAnnotatedMethod(hc.cp, method, annotation, link.mh));
            } else {
                Hook.Builder<?> builder = builderOf(hr.array, link.index);
                AnnotatedMethodHook<Annotation> hook = ModuleAccess.hook().newAnnotatedMethodHook(hc, method, annotation);
                link.mh.invoke(builder, hook);
            }
        }
    }

    private static class Link {
        private final int index;
        private final MethodHandle mh;
        @Nullable
        private final Link next;

        private Link(MethodHandle mh, int index, @Nullable Link next) {
            this.mh = requireNonNull(mh);
            this.index = index;
            this.next = next;
        }

    }
}
