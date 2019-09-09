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
package packed.internal.container.model;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.Extension;
import app.packed.container.extension.HookGroupBuilder;
import app.packed.container.extension.AnnotatedMethodHook;
import packed.internal.access.SharedSecrets;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.container.extension.OnHookXModel;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.container.extension.hook.ExtensionCallback;
import packed.internal.container.extension.hook.HookGroupBuilderModel;
import packed.internal.util.ThrowableUtil;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
// One of these suckers is creates once for each component...
final class ComponentModelHookGroup {

    /** A list of callbacks for the particular extension. */
    private final List<ExtensionCallback> callbacks;

    /** The type of extension that will be activated. */
    private final Class<? extends Extension> extensionType;

    private ComponentModelHookGroup(Builder builder) {
        this.extensionType = requireNonNull(builder.extensionType);
        this.callbacks = List.copyOf(builder.callbacks);
    }

    void addTo(PackedContainerConfiguration container, ComponentConfiguration component) throws Throwable {
        // There should probably be some order we call extensions in....
        /// Other first, packed lasts?

        PackedExtensionContext context = container.useContext(extensionType); // should be ordered...s
        for (ExtensionCallback c : callbacks) {
            c.invoke(context, component);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static class Builder {

        private final ArrayList<ExtensionCallback> callbacks = new ArrayList<>();

        /** The component model builder that is creating this group. */
        private final ComponentModel.Builder componentModelBuilder;

        final OnHookXModel con;

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        final IdentityHashMap<Class<?>, HookGroupBuilder<?>> groupBuilders = new IdentityHashMap<>();

        final ExtensionModel<?> extensionModel;

        Builder(ComponentModel.Builder componentModelBuilder, Class<? extends Extension> extensionType) {
            this.componentModelBuilder = requireNonNull(componentModelBuilder);
            this.extensionType = requireNonNull(extensionType);
            this.extensionModel = ExtensionModel.of(extensionType);
            this.con = extensionModel.model();
        }

        ComponentModelHookGroup build() {
            for (Entry<Class<?>, HookGroupBuilder<?>> m : groupBuilders.entrySet()) {
                MethodHandle mh = con.groups.get(m.getKey());
                callbacks.add(new ExtensionCallback(mh, m.getValue().build()));
            }
            return new ComponentModelHookGroup(this);
        }

        void onAnnotatedField(Field field, Annotation annotation) {
            AnnotatedFieldHook<Annotation> hook = SharedSecrets.extension().newAnnotatedFieldHook(componentModelBuilder, field, annotation);
            process(con.findMethodHandleForAnnotatedField(hook), hook);
        }

        void onAnnotatedMethod(Method method, Annotation annotation) {
            AnnotatedMethodHook hook = SharedSecrets.extension().newAnnotatedMethodHook(componentModelBuilder, method, annotation);
            process(con.findMethodHandleForAnnotatedMethod(hook), hook);
        }

        private void process(MethodHandle mh, Object hook) {
            Class<?> owner = mh.type().parameterType(0);
            if (owner == extensionType) {
                callbacks.add(new ExtensionCallback(mh, hook));
                throw new Error();
            } else {
                processBuilder(mh, (Class<? extends HookGroupBuilder<?>>) owner, hook);
            }
        }

        private void processBuilder(MethodHandle mh, Class<? extends HookGroupBuilder<?>> builderType, Object hook) {
            HookGroupBuilder<?> b = groupBuilders.computeIfAbsent(builderType, k -> HookGroupBuilderModel.newInstance(builderType));
            try {
                mh.invoke(b, hook);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new RuntimeException(e);
            }
        }
    }

}
