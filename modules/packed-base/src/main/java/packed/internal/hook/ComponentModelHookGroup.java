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
import java.lang.reflect.UndeclaredThrowableException;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.AnnotatedTypeHook;
import app.packed.hook.Hook;
import app.packed.lang.Nullable;
import packed.internal.component.ComponentModel;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.hook.model.OnHookContainerModel;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.util.ThrowableUtil;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
// One of these suckers is creates once for each component+Extension combination...
public final class ComponentModelHookGroup {

    /** A list of callbacks for the particular extension. */
    private final CachedHook<Hook> callback;

    /** The type of extension that will be activated. */
    private final Class<? extends Extension> extensionType;

    private ComponentModelHookGroup(Builder builder) {
        this.extensionType = requireNonNull(builder.extensionType);
        this.callback = builder.callback;
    }

    public void addTo(PackedContainerConfiguration container, ComponentConfiguration<?> cc) throws Throwable {

        // First make sure the extension is activated
        Extension e = container.use(extensionType);

        // Call the actual methods on the Extension
        for (CachedHook<Hook> c = callback; c != null; c = c.next()) {
            c.mh().invoke(e, c.hook(), cc);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final class Builder {

        @Nullable
        private CachedHook<Hook> callback;

        /** The component model builder that is creating this group. */
        private final ComponentModel.Builder componentModelBuilder;

        final HookContainerModel oldHooks;

        final OnHookContainerModel hooks;

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        final IdentityHashMap<Class<?>, Hook.Builder<?>> groupBuilders = new IdentityHashMap<>();

        final Object[] array;

        public Builder(ComponentModel.Builder componentModelBuilder, Class<? extends Extension> extensionType) {
            this.componentModelBuilder = requireNonNull(componentModelBuilder);
            this.extensionType = requireNonNull(extensionType);
            this.oldHooks = ExtensionModel.of(extensionType).hooks();
            this.hooks = ExtensionModel.of(extensionType).hooks2();

            array = new Object[hooks.size()];

            // hooks.
        }

        public ComponentModelHookGroup build() {
            for (Entry<Class<?>, Hook.Builder<?>> m : groupBuilders.entrySet()) {
                MethodHandle mh = oldHooks.groups.get(m.getKey());
                callback = new CachedHook(mh, m.getValue().build(), callback);
            }
            hooks.compute(array);
            return new ComponentModelHookGroup(this);
        }

        public void onAnnotatedType(Class<?> clazz, Annotation annotation) {
            AnnotatedTypeHook<Annotation> hook = ModuleAccess.hook().newAnnotatedTypeHook(componentModelBuilder.hookController, clazz, annotation);
            process(oldHooks.findMethodHandleForAnnotatedType(hook), hook);
        }

        public void onAnnotatedField(Field field, Annotation annotation) {
            AnnotatedFieldHook<Annotation> hook = ModuleAccess.hook().newAnnotatedFieldHook(componentModelBuilder.hookController, field, annotation);
            process(oldHooks.findMethodHandleForAnnotatedField(hook), hook);
            hooks.tryProcesAnnotatedField(componentModelBuilder.hookController, field, annotation, array);
        }

        public void onAnnotatedMethod(Method method, Annotation annotation) {
            AnnotatedMethodHook hook = ModuleAccess.hook().newAnnotatedMethodHook(componentModelBuilder.hookController, method, annotation);
            process(oldHooks.findMethodHandleForAnnotatedMethod(hook), hook);
            hooks.tryProcesAnnotatedMethod(componentModelBuilder.hookController, method, annotation, array);
        }

        private void process(MethodHandle mh, Hook hook) {
            Class<?> owner = mh.type().parameterType(0);
            if (owner == extensionType) {
                // callbacks.add(new HookCallback(mh, hook, null));
                throw new Error();
            } else {
                processBuilder(mh, (Class<? extends Hook.Builder<?>>) owner, hook);
            }
        }

        private void processBuilder(MethodHandle mh, Class<? extends Hook.Builder<?>> builderType, Hook hook) {
            Hook.Builder<?> b = groupBuilders.computeIfAbsent(builderType, k -> HookBuilderModel.newInstance(builderType));
            try {
                mh.invoke(b, hook);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }
}
