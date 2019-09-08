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
package packed.internal.container.extension.hook;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.Extension;
import app.packed.container.extension.HookGroupBuilder;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.model.ComponentModel;
import packed.internal.util.ThrowableUtil;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
public final class ComponentModelHookGroup {

    /** A list of callbacks for the particular extension. */
    private final List<ExtensionCallback> callbacks;

    /** The type of extension that will be activated. */
    private final Class<? extends Extension> extensionType;

    private ComponentModelHookGroup(Builder b) {
        this.extensionType = requireNonNull(b.extensionType);
        this.callbacks = b.callbacks;
    }

    public void addTo(PackedContainerConfiguration container, ComponentConfiguration component) {
        Extension extension = container.use(extensionType); // should be ordered...s
        try {
            for (ExtensionCallback c : callbacks) {
                c.mh.invoke(extension, component, c.hookGroup);
            }
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    public int getNumberOfCallbacks() {
        return callbacks.size();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static class Builder {

        final ArrayList<ExtensionCallback> callbacks = new ArrayList<>();

        /** The component type */
        final Class<?> componentType;

        final OnHookXModel con;

        /** The type of extension that will be activated. */
        final Class<? extends Extension> extensionType;

        final IdentityHashMap<Class<?>, HookGroupBuilder<?>> mmm = new IdentityHashMap<>();

        final ComponentModel.Builder modelBuilder;

        public Builder(ComponentModel.Builder modelBuilder, Class<? extends Extension> extensionType) {
            this.modelBuilder = requireNonNull(modelBuilder);
            this.componentType = modelBuilder.componentType();
            this.con = OnHookXModel.get(extensionType);
            this.extensionType = requireNonNull(extensionType);
        }

        public ComponentModelHookGroup build() {
            // Add all aggregates
            for (Entry<Class<?>, HookGroupBuilder<?>> m : mmm.entrySet()) {
                MethodHandle mh = con.aggregators.get(m.getKey());
                callbacks.add(new ExtensionCallback(mh, m.getValue().build()));
            }
            return new ComponentModelHookGroup(this);
        }

        public void onAnnotatedField(Field field, Annotation annotation) {
            PackedAnnotatedFieldHook hook = new PackedAnnotatedFieldHook(modelBuilder, field, annotation);
            process(con.findMethodHandleForAnnotatedField(hook), hook);
        }

        public void onAnnotatedMethod(Method method, Annotation annotation) {
            PackedAnnotatedMethodHook hook = new PackedAnnotatedMethodHook(modelBuilder, method, annotation);
            process(con.findMethodHandleForAnnotatedMethod(hook), hook);
        }

        private void process(MethodHandle mh, Object hook) {
            Class<?> owner = mh.type().parameterType(0);
            if (owner == extensionType) {
                callbacks.add(new ExtensionCallback(mh, hook));
            } else {
                // The method handle refers to an aggregator object.
                HookGroupBuilderModel a = HookGroupBuilderModel.of((Class<? extends HookGroupBuilder<?>>) owner);
                HookGroupBuilder<?> sup = mmm.computeIfAbsent(owner, k -> a.newInstance());
                try {
                    mh.invoke(sup, hook);
                } catch (Throwable e) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
