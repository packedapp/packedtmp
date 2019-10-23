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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.Hook;
import app.packed.lang.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.hook.model.CachedHook;
import packed.internal.hook.model.HookProcessor;
import packed.internal.hook.model.HookRequest;
import packed.internal.util.ThrowableUtil;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
// One of these suckers is creates once for each component+Extension combination...
final class ComponentModelHookGroup extends HookRequest {

    /** The type of extension that will be activated. */
    final Class<? extends Extension> extensionType;

    private ComponentModelHookGroup(HookRequest.Builder builder, Class<? extends Extension> extensionType) throws Throwable {
        super(builder);
        this.extensionType = requireNonNull(extensionType);
    }

    void process(PackedContainerConfiguration containerConfiguration, ComponentConfiguration<?> componentConfiguration) throws Throwable {
        // First make sure the extension is activated
        Extension e = containerConfiguration.use(extensionType);

        // Call the actual methods on the Extension
        for (CachedHook<Hook> c = customHooksCallback; c != null; c = c.next()) {
            MethodHandle mh = c.mh();
            if (mh.type().parameterCount() == 2) {
                c.mh().invoke(e, c.hook());
            } else {
                c.mh().invoke(e, c.hook(), componentConfiguration);
            }
        }
    }

    static final class Builder extends HookRequest.Builder {

        @Nullable
        private CachedHook<Hook> callback;

        private final HookProcessor hookProcessor;

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        public Builder(HookProcessor hookProcessor, Class<? extends Extension> extensionType) {
            super(ExtensionModel.of(extensionType).hooks());
            this.hookProcessor = requireNonNull(hookProcessor);
            this.extensionType = requireNonNull(extensionType);
        }

        public ComponentModelHookGroup build() {
            try {
                return new ComponentModelHookGroup(this, extensionType);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }

        public void onAnnotatedType(Class<?> clazz, Annotation annotation) {
            throw new UnsupportedOperationException();
        }

        public void onAnnotatedField(Field field, Annotation annotation) throws Throwable {
            onAnnotatedField(hookProcessor, field, annotation);
        }

        public void onAnnotatedMethod(Method method, Annotation annotation) throws Throwable {
            onAnnotatedMethod(hookProcessor, method, annotation);
        }
    }
}
