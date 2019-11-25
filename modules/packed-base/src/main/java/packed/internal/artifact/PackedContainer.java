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

import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;

import app.packed.artifact.ArtifactContext;
import app.packed.lang.Key;
import app.packed.lang.Nullable;
import app.packed.service.Injector;
import packed.internal.component.AbstractComponent;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.service.run.DefaultInjector;

/** The default implementation of Container. */
// implements ContainerContext...
/// Which is available to every component in the container.....
/// Maybe via ComponentContext
/// Just worried

/// Naehhhh, Det er jo en container....
// Man skal ikke kunne iterere igennem den jo...

public final class PackedContainer extends AbstractComponent implements ArtifactContext {

    private final Injector injector;

    /**
     * Creates a new container
     * 
     * @param parent
     *            the parent of the container if it is a non-root container
     * @param configuration
     *            the configuration of the container
     * @param instantiationContext
     *            the instantiation context of the container
     */
    public PackedContainer(@Nullable AbstractComponent parent, PackedContainerConfiguration configuration,
            PackedArtifactInstantiationContext instantiationContext) {
        super(parent, configuration, instantiationContext);
        Injector i = instantiationContext.get(configuration, DefaultInjector.class);
        if (i == null) {
            i = new DefaultInjector(configuration.configSite(), configuration.getDescription(), new LinkedHashMap<>());
        }
        this.injector = i;
        instantiationContext.put(configuration, this);
    }

    @Override
    public Injector injector() {
        return injector;
    }

    @Override
    public <T> T use(Key<T> key) {
        return injector.use(key);
    }

    public <T> CompletableFuture<T> asyncStart(T result) {
        // Taenkt saa man kan kalde
        // CompletableFuture<App> cf = artifact.asyncStart(this);
        throw new UnsupportedOperationException();
    }
}
