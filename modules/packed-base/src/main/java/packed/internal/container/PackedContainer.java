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

import app.packed.container.InstantiationContext;
import app.packed.inject.Injector;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.DefaultInjector;

/** The default implementation of Container. */
public final class PackedContainer extends AbstractComponent {

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
    PackedContainer(@Nullable AbstractComponent parent, PackedContainerConfiguration configuration, InstantiationContext instantiationContext) {
        super(parent, configuration, instantiationContext);
        Injector i = instantiationContext.get(configuration, DefaultInjector.class);
        if (i == null) {
            i = new DefaultInjector(configuration, new ServiceNodeMap());
        }
        this.injector = i;
        instantiationContext.put(configuration, this);
    }

    public Injector injector() {
        return injector;
    }

    public <T> T use(Class<T> key) {
        return injector.use(key);
    }
}
