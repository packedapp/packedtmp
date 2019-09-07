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

import java.util.concurrent.CompletableFuture;

import app.packed.artifact.ArtifactRuntimeContext;
import app.packed.inject.Injector;
import app.packed.util.Nullable;
import packed.internal.inject.run.DefaultInjector;
import packed.internal.inject.util.ServiceNodeMap;

/** The default implementation of Container. */
// implements ContainerContext...
/// Which is available to every component in the container.....
/// Maybe via ComponentContext
/// Just worried
public final class PackedArtifactContext extends AbstractComponent implements ArtifactRuntimeContext {

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
    PackedArtifactContext(@Nullable AbstractComponent parent, PackedContainerConfiguration configuration,
            PackedArtifactInstantiationContext instantiationContext) {
        super(parent, configuration, instantiationContext);
        Injector i = instantiationContext.get(configuration, DefaultInjector.class);
        if (i == null) {
            i = new DefaultInjector(configuration.configSite(), configuration.getDescription(), new ServiceNodeMap());
        }
        this.injector = i;
        instantiationContext.put(configuration, this);
    }

    @Override
    public Injector injector() {
        return injector;
    }

    @Override
    public <T> T use(Class<T> key) {
        return injector.use(key);
    }

    public <T> CompletableFuture<T> asyncStart(T result) {
        // Taenkt saa man kan kalde
        // CompletableFuture<App> cf = artifact.asyncStart(this);
        throw new UnsupportedOperationException();
    }
}
