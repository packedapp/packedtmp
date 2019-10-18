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
package packed.internal.artifact;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;

import app.packed.container.ContainerConfiguration;
import app.packed.container.extension.Extension;
import app.packed.container.extension.InternalExtensionException;
import app.packed.container.extension.ExtensionInstantiationContext;
import app.packed.container.extension.ExtensionWirelet.Pipeline;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.WireletContext;
import packed.internal.container.extension.PackedExtensionContext;

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
 * The main difference from {@link ArtifactBuildContext} is when using an artifact image
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> Hmmm what about if use them at startup???
 */
// ArtifactInstantiationContext or ContainerInstantionationContext
// Per Artifact or PerContainer???

// Per container, er sgu for besvaergeligt med de der get stuff...
// Altsaa med mindre vi har behov for at access dem fra andet sted fra
public final class PackedArtifactInstantiationContext {

    /** All context objects. */
    private final IdentityHashMap<ContainerConfiguration, IdentityHashMap<Class<?>, Object>> map = new IdentityHashMap<>();

    public final IdentityHashMap<Class<? extends Extension>, ExtensionInstantiationContext> instContexts = new IdentityHashMap<>();

    public final WireletContext wirelets;

    public PackedArtifactInstantiationContext(WireletContext wirelets) {
        this.wirelets = wirelets;
    }

    public ExtensionInstantiationContext newContext(PackedContainerConfiguration pcc, PackedExtensionContext e) {
        return new ExtensionInstantiationContext() {
            @Override
            public <T extends Pipeline<?, ?, ?>> T getPipeline(Class<T> pipelineType) {
                // We need to check that someone does not request another extensions pipeline type.
                if (!e.model().pipelines.containsKey(pipelineType)) {
                    throw new InternalExtensionException(
                            "The specified pipeline type is not amongst " + e.type().getSimpleName() + " pipeline types, pipelineType = " + pipelineType);
                }
                return wirelets.getPipelin(pipelineType);
            }

            @Override
            public Class<?> artifactType() {
                return PackedArtifactInstantiationContext.this.artifactType();
            }

            @Nullable
            @Override
            public <T> T get(Class<T> type) {
                return PackedArtifactInstantiationContext.this.get(pcc, type);
            }

            @Override
            public void put(Object obj) {
                PackedArtifactInstantiationContext.this.put(pcc, obj);
            }

            @Override
            public <T> T use(Class<T> type) {
                return PackedArtifactInstantiationContext.this.use(pcc, type);
            }

            @Override
            public boolean isFromImage() {
                return false;
            }
        };
    }

    /**
     * Returns the type of artifact the build process produces.
     * 
     * @return the type of artifact the build process produces
     */
    public Class<?> artifactType() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(ContainerConfiguration configuration, Class<T> type) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(type, "type is null");
        var e = map.get(configuration);
        return e == null ? null : (T) e.get(type);
    }

    public void put(ContainerConfiguration configuration, Object obj) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(obj, "obj is null");
        map.computeIfAbsent(configuration, e -> new IdentityHashMap<>()).put(obj.getClass(), obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T use(ContainerConfiguration configuration, Class<T> type) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(type, "type is null");
        var e = map.get(configuration);
        if (e == null) {
            throw new IllegalArgumentException();
        }
        Object o = e.get(type);
        if (o == null) {
            throw new IllegalStateException();
        }
        return (T) o;
    }

    /**
     * Returns a list of wirelets that used to instantiate. This may include wirelets that are not present at build time if
     * using an image.
     * 
     * @return a list of wirelets that used to instantiate
     */
    public WireletContext wirelets() {
        return wirelets;
    }

    // link(SomeBundle.class, LifecycleWirelets.startBeforeAnythingElse());
    // link(SomeBundle.class, LifecycleWirelets.start(SomeGroup)); //all in same group will be started
}
