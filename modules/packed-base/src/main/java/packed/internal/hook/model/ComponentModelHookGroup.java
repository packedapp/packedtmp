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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.Hook;
import app.packed.lang.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.ExtensionModel;

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

    public static final class Builder {

        @Nullable
        private CachedHook<Hook> callback;

        final OnHookContainerModel hooks;

        final HookProcessor hookProcessor;
        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        final Object[] array;

        public Builder(HookProcessor hookProcessor, Class<? extends Extension> extensionType) {
            this.hookProcessor = requireNonNull(hookProcessor);
            this.extensionType = requireNonNull(extensionType);
            this.hooks = ExtensionModel.of(extensionType).hooks2();
            array = new Object[hooks.size()];
        }

        public ComponentModelHookGroup build() {
            callback = hooks.compute(array);
            return new ComponentModelHookGroup(this);
        }

        public void onAnnotatedType(Class<?> clazz, Annotation annotation) {
            throw new UnsupportedOperationException();
        }

        public void onAnnotatedField(Field field, Annotation annotation) {
            hooks.tryProcesAnnotatedField(hookProcessor, field, annotation, array);
        }

        public void onAnnotatedMethod(Method method, Annotation annotation) {
            hooks.tryProcesAnnotatedMethod(hookProcessor, method, annotation, array);
        }
    }
}
