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

import java.util.IdentityHashMap;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.Hook;
import app.packed.lang.Nullable;
import packed.internal.component.ComponentModel;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.ExtensionModel;
import packed.internal.hook.CachedHook;
import packed.internal.hook.HookContainerModel;

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

        /** The component model builder that is creating this group. */
        final ComponentModel.Builder componentModelBuilder;

        final HookContainerModel con;

        /** The type of extension that will be activated. */
        private final Class<? extends Extension> extensionType;

        final IdentityHashMap<Class<?>, Hook.Builder<?>> groupBuilders = new IdentityHashMap<>();

        public Builder(ComponentModel.Builder componentModelBuilder, Class<? extends Extension> extensionType) {
            this.componentModelBuilder = requireNonNull(componentModelBuilder);
            this.extensionType = requireNonNull(extensionType);
            this.con = ExtensionModel.of(extensionType).hooks();
        }

    }
}
