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

import java.lang.reflect.UndeclaredThrowableException;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.Hook;
import app.packed.lang.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.hook.CachedHook;
import packed.internal.hook.HookProcessor;
import packed.internal.hook.HookRequest;
import packed.internal.util.ThrowableUtil;

/**
 * We have a group for a collection of hooks/annotations. A component can have multiple groups.
 */
// One of these suckers is creates once for each component+Extension combination...
final class ComponentHookRequest extends HookRequest {

    /** The type of extension that will be activated. */
    final Class<? extends Extension> extensionType;

    private ComponentHookRequest(HookRequest.Builder builder, Class<? extends Extension> extensionType) throws Throwable {
        super(builder);
        this.extensionType = requireNonNull(extensionType);
    }

    void process(PackedContainerConfiguration containerConfiguration, ComponentConfiguration<?> componentConfiguration) throws Throwable {
        Extension e = containerConfiguration.use(extensionType);
        invokeIt(e, componentConfiguration);
    }

    static final class Builder extends HookRequest.Builder {

        @Nullable
        private CachedHook<Hook> callback;

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        public Builder(HookProcessor hookProcessor, Class<? extends Extension> extensionType) {
            super(ExtensionModel.of(extensionType).hooks(), hookProcessor);
            this.extensionType = requireNonNull(extensionType);
        }

        public ComponentHookRequest build() {
            try {
                return new ComponentHookRequest(this, extensionType);
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }
}
