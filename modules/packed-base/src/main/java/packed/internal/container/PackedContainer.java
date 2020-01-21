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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import app.packed.artifact.ArtifactContext;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.component.ComponentType;
import app.packed.component.FeatureMap;
import app.packed.config.ConfigSite;
import app.packed.container.Container;
import app.packed.container.Extension;
import app.packed.lifecycle.StopOption;
import app.packed.service.Injector;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.component.AbstractComponent;
import packed.internal.service.run.DefaultInjector;

/** The default implementation of {@link Container}. */
public final class PackedContainer extends AbstractComponent implements Container {

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
    PackedContainer(@Nullable AbstractComponent parent, PackedContainerConfiguration pcc, PackedArtifactInstantiationContext instantiationContext) {
        super(parent, pcc, instantiationContext);
        Injector i = instantiationContext.get(pcc, DefaultInjector.class);
        if (i == null) {
            i = new DefaultInjector(pcc.configSite(), pcc.getDescription(), new LinkedHashMap<>());
        }
        this.injector = i;
        instantiationContext.put(pcc, this);
    }

    public ArtifactContext newArtifactContext() {
        return new ArtifactContextWrapper();
    }

    /** Used to expose a container as an ArtifactContext. */
    public class ArtifactContextWrapper implements ArtifactContext {

        /** {@inheritDoc} */
        @Override
        public Collection<Component> children() {
            return PackedContainer.this.children();
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configSite() {
            return PackedContainer.this.configSite();
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return PackedContainer.this.depth();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> description() {
            return PackedContainer.this.description();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension>> extension() {
            return PackedContainer.this.extension();
        }

        /** {@inheritDoc} */
        @Override
        public FeatureMap features() {
            return PackedContainer.this.features();
        }

        /** {@inheritDoc} */
        @Override
        public Injector injector() {
            return PackedContainer.this.injector;
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return PackedContainer.this.name();
        }

        /** {@inheritDoc} */
        @Override
        public ComponentPath path() {
            return PackedContainer.this.path();
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
            return PackedContainer.this.stream(options);
        }

        /** {@inheritDoc} */
        @Override
        public ComponentType type() {
            return PackedContainer.this.type();
        }

        /** {@inheritDoc} */
        @Override
        public <T> T use(Key<T> key) {
            return injector.use(key);
        }

        /** {@inheritDoc} */
        @Override
        public Component useComponent(CharSequence path) {
            return PackedContainer.this.useComponent(path);
        }

        /** {@inheritDoc} */
        @Override
        public void traverse(Consumer<? super Component> action) {
            PackedContainer.this.traverse(action);
        }
    }
}
