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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.hook.model.OnHookContainerModelBuilder.LinkedEntry;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public final class OnHookContainerModel {

    /** Constructors for each builder. */
    private final MethodHandle[] constructors;

    /** Methods annotated with {@link OnHook} that takes a non-base {@link Hook}. */
    private final Link[] customHooks;

    final ImmutanleOnHookMap<Link> allLinks;

    final ImmutanleOnHookMap<Link> rootLinks;

    OnHookContainerModel(OnHookContainerModelBuilder b) {
        Function<OnHookContainerModelBuilder.LinkedEntry, Link> ff = e -> {
            Link l = null;
            for (; e != null; e = e.next) {
                l = new Link(e.methodHandle, e.builder.index, l);
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
        this.constructors = new MethodHandle[b.stack.size()];
        // for (int i = 0; i < b.result.size(); i++) {
        // OnHookContainerModelBuilder.Node n = b.result.get(i);
        // String msg = i + " " + n.index + " " + n.onNodeContainerType;
        // if (n.builderConstructor != null) {
        // msg += " " + n.builderConstructor.type().returnType();
        // }
        // System.out.println(msg);
        // }
        // System.out.println("-----");
        List<OnHookContainerModelBuilder.Node> list = List.copyOf(b.stack);
        for (int i = 0; i < list.size(); i++) {
            OnHookContainerModelBuilder.Node n = list.get(i);// b.result.get(i);
            constructors[i] = n.builderConstructor;
            if (b.allEntries.customHooks != null) {
                // We reverse the order here so instead of Dependent->Dependency we get Dependency->Dependent
                // We do this so we do not automatically invoke methods on the root object. which is never cached.
                for (LinkedEntry l = b.allEntries.customHooks.get(n.onNodeContainerType); l != null; l = l.next) {
                    customHooks[l.builder.index] = new Link(l.methodHandle, i, customHooks[l.builder.index]);
                }
            }
        }
    }

    /**
     * Returns an immutable set of all field triggering annotations types.
     * 
     * @return the set
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Set<Class<? extends Annotation>> annotatedFieldHookTypes() {
        Map<Class<?>, Link> a = allLinks.annotatedFields;
        return a == null ? Set.of() : (Set) a.keySet();
    }

    public int size() {
        return constructors.length;
    }

    public void tryProcesAnnotatedField(HookProcessor hc, Field f, Annotation a, Object[] array) {
        for (Link link = allLinks.annotatedFields.get(a.annotationType()); link != null; link = link.next) {
            Object builder = builderOf(this, link.index, array);
            AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(hc, f, a);
            try {
                link.mh.invoke(builder, hook);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    public CachedHook<Hook> compute(Object[] array) {
        for (int i = array.length - 1; i >= 0; i--) {
            for (Link link = customHooks[i]; link != null; link = link.next) {
                if (constructors[i] != null) {
                    Object builder = builderOf(this, i, array);
                    try {
                        link.mh.invoke(builder, array[link.index]);
                    } catch (Throwable e1) {
                        ThrowableUtil.rethrowErrorOrRuntimeException(e1);
                        throw new UndeclaredThrowableException(e1);
                    }
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

    public void tryProcesAnnotatedMethod(HookProcessor hc, Method m, Annotation a, Object[] array) {
        for (Link link = allLinks.annotatedMethods.get(a.annotationType()); link != null; link = link.next) {
            Object builder = builderOf(this, link.index, array);
            AnnotatedMethodHook<Annotation> hook = ModuleAccess.hook().newAnnotatedMethodHook(hc, m, a);
            try {
                link.mh.invoke(builder, hook);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    @Nullable
    public Object process(@Nullable Object parent, ClassProcessor cpTarget, UncheckedThrowableFactory<?> tf) {
        Object[] array = new Object[constructors.length];
        array[0] = parent;
        HookProcessor hc = new HookProcessor(cpTarget, tf);
        cpTarget.findMethodsAndFields(allLinks.annotatedMethods == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                tryProcesAnnotatedMethod(hc, f, a, array);
            }
        }, allLinks.annotatedFields == null ? null : f -> {
            for (Annotation a : f.getAnnotations()) {
                tryProcesAnnotatedField(hc, f, a, array);
            }
        });
        hc.close();

        // Process everything but the top elements, which we do in the end.
        for (int i = array.length - 1; i >= 0; i--) {
            for (Link link = customHooks[i]; link != null; link = link.next) {
                Object builder = builderOf(this, i, array);
                try {
                    link.mh.invoke(builder, array[link.index]);
                } catch (Throwable e1) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e1);
                    throw new UndeclaredThrowableException(e1);
                }
            }
            if (i > 0) {
                Object h = array[i];
                if (h != null) {
                    array[i] = ((Hook.Builder<?>) h).build();
                }
            }
        }
        if (parent != null) {
            return parent;
        }
        Object a = array[0];
        return a == null ? null : ((Hook.Builder<?>) a).build();
    }

    private static Object builderOf(OnHookContainerModel m, int index, Object[] array) {
        requireNonNull(m.constructors[index]);
        Object builder = array[index];
        if (builder == null) {
            try {
                builder = array[index] = m.constructors[index].invoke();
            } catch (Throwable e2) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e2);
                throw new UndeclaredThrowableException(e2);
            }
        }
        return builder;
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
