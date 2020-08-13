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

import app.packed.base.Nullable;
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
public final class InstantiationContext {

    /** All context objects. */
    private final IdentityHashMap<ComponentNodeConfiguration, IdentityHashMap<Class<?>, Object>> map = new IdentityHashMap<>();

    public final WireletPack wirelets;

    public InstantiationContext(WireletPack wirelets) {
        this.wirelets = wirelets;
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
    public <T> T get(ComponentNodeConfiguration configuration, Class<T> type) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(type, "type is null");
        var e = map.get(configuration);
        return e == null ? null : (T) e.get(type);
    }

    public void put(ComponentNodeConfiguration configuration, Object obj) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(obj, "obj is null");
        map.computeIfAbsent(configuration, e -> new IdentityHashMap<>()).put(obj.getClass(), obj);
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

    // link(SomeBundle.class, LifecycleWirelets.startBeforeAnythingElse());
    // link(SomeBundle.class, LifecycleWirelets.start(SomeGroup)); //all in same group will be started
}
// @SuppressWarnings("unchecked")
// public <T> T use(ContainerConfiguration configuration, Class<T> type) {
// requireNonNull(configuration, "configuration is null");
// requireNonNull(type, "type is null");
// var e = map.get(configuration);
// if (e == null) {
// throw new IllegalArgumentException();
// }
// Object o = e.get(type);
// if (o == null) {
// throw new IllegalStateException();
// }
// return (T) o;
// }
