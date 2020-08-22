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
package packed.internal.lifecycle.phases;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;

import app.packed.artifact.GuestContext;
import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.lifecycleold.StopOption;
import app.packed.service.Injector;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.wirelet.WireletPack;

/**
 * An instantiation context is created for every delimited tree hierachy.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */
// ArtifactInstantiationContext

/**
 * An artifact instantiation context is created every time an artifact is being instantiated.
 * <p>
 * Describes which phases it is available from
 * <p>
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> Hmmm what about if use them at startup???
 */
// ArtifactInstantiationContext or ContainerInstantionationContext
// Per Artifact or PerContainer???
// Per container, er sgu for besvaergeligt med de der get stuff...
// Altsaa med mindre vi har behov for at access dem fra andet sted fra

public final class ConstructionContext {

    private final WireletPack wirelets;

    private ConstructionContext(WireletPack wirelets) {
        this.wirelets = wirelets;
    }

    /**
     * Returns a list of wirelets that used to instantiate. This may include wirelets that are not present at build time if
     * using an image.
     * 
     * @return a list of wirelets that used to instantiate
     */
    public WireletPack wirelets() {
        return wirelets;
    }

    public static GuestContext constructArtifact(ComponentNodeConfiguration root, WireletPack wp) {
        ConstructionContext ic = new ConstructionContext(wp);
        // Will instantiate the whole container hierachy
        ComponentNode node = root.instantiateTree(ic);

        // TODO run initialization

        return new PackedGuestContext(node);
    }

    /** Used to expose a container as an ArtifactContext. */
    public static final class PackedGuestContext implements GuestContext {

        /** The component node we are wrapping. */
        private final ComponentNode node;

        private PackedGuestContext(ComponentNode node) {
            this.node = requireNonNull(node);
        }

        /** {@inheritDoc} */
        @Override
        public Injector injector() {
            return (Injector) node.data[0];
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
        public <T> T use(Key<T> key) {
            return injector().use(key);
        }

        /** {@inheritDoc} */
        @Override
        public Component component() {
            return node;
        }
    }

}
