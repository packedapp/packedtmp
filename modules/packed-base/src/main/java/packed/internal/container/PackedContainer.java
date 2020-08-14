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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import app.packed.artifact.ArtifactContext;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.lifecycleold.StopOption;
import app.packed.service.Injector;
import packed.internal.artifact.InstantiationContext;
import packed.internal.component.ComponentNode;
import packed.internal.service.runtime.PackedInjector;

/** The default container implementation. */
public final class PackedContainer {

    public static ComponentNode create(@Nullable ComponentNode parent, PackedContainerRole pcc, InstantiationContext instantiationContext) {
        ComponentNode cn = new ComponentNode(parent, pcc.component, instantiationContext);
        Injector i = instantiationContext.get(pcc.component, PackedInjector.class);
        if (i == null) {
            i = new PackedInjector(pcc.component.configSite(), pcc.component.getDescription(), new LinkedHashMap<>());
        }
        cn.data[0] = i;
        instantiationContext.put(pcc.component, cn);
        return cn;
    }

    /** Used to expose a container as an ArtifactContext. */
    public static final class PackedArtifactContext implements ArtifactContext {

        private final ComponentNode container;

        public PackedArtifactContext(ComponentNode container) {
            this.container = requireNonNull(container);
        }

        public Collection<Component> children() {
            return container.children();
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configSite() {
            return container.configSite();
        }

        public int depth() {
            return container.depth();
        }

        /** {@inheritDoc} */
        @Override
        public Injector injector() {
            return (Injector) container.data[0];
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return container.name();
        }

        public Optional<Component> parent() {
            return container.parent();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPath path() {
            return container.path();
        }

        /** {@inheritDoc} */
        @Override
        public void stop(StopOption... options) {

        }

        /** {@inheritDoc} */
        @Override
        public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public ComponentStream stream(Option... options) {
            return container.stream(options);
        }

        /** {@inheritDoc} */
        @Override
        public <T> T use(Key<T> key) {
            return injector().use(key);
        }

        /** {@inheritDoc} */
        @Override
        public Component useComponent(CharSequence path) {
            return container.useComponent(path);
        }

        public ComponentRelation relationTo(Component other) {
            return container.relationTo(other);
        }
    }
}
