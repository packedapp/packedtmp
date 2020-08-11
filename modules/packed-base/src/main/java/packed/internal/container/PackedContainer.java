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
import app.packed.container.Extension;
import app.packed.lifecycleold.StopOption;
import app.packed.service.Injector;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.component.PackedComponent;
import packed.internal.service.runtime.PackedInjector;

/** The default container implementation. */
public final class PackedContainer extends PackedComponent {

    // Skal vaere en service of some kind...
    private final Injector injector;

    /**
     * Creates a new container
     * 
     * @param parent
     *            the parent of the container if it is a non-root container
     * @param pcc
     *            the configuration of the container
     * @param instantiationContext
     *            the instantiation context of the container
     */
    public PackedContainer(@Nullable PackedComponent parent, PackedContainerConfigurationContext pcc, PackedInstantiationContext instantiationContext) {
        super(parent, pcc, instantiationContext);
        Injector i = instantiationContext.get(pcc, PackedInjector.class);
        if (i == null) {
            i = new PackedInjector(pcc.configSite(), pcc.getDescription(), new LinkedHashMap<>());
        }
        this.injector = i;
        instantiationContext.put(pcc, this);
    }

    public ArtifactContext toArtifactContext() {
        return new PackedArtifactContext(this);
    }

    /** Used to expose a container as an ArtifactContext. */
    public static final class PackedArtifactContext implements ArtifactContext, Component {

        private final PackedContainer container;

        PackedArtifactContext(PackedContainer container) {
            this.container = requireNonNull(container);
        }

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            return container.children();
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configSite() {
            return container.configSite();
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return container.depth();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> description() {
            return container.description();
        }

        public Optional<Class<? extends Extension>> extension() {
            return container.extension();
        }

        /** {@inheritDoc} */
        @Override
        public Injector injector() {
            return container.injector;
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return container.name();
        }

        /** {@inheritDoc} */
        @Override
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
            return container.injector.use(key);
        }

        /** {@inheritDoc} */
        @Override
        public Component useComponent(CharSequence path) {
            return container.useComponent(path);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentRelation relationTo(Component other) {
            return container.relationTo(other);
        }
    }
}
