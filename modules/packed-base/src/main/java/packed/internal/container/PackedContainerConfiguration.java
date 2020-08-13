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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.inject.Factory;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    /** The context to delegate all calls to. */
    private final PackedContainerConfigurationContext context;

    public PackedContainerConfiguration(PackedContainerConfigurationContext context) {
        super(context);
        this.context = context;
    }

    /** {@inheritDoc} */
    @Override
    public <W extends Wirelet> Optional<W> assemblyWirelet(Class<W> type) {
        return context.assemblyWirelet(type);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> extensions() {
        return context.extensions();
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Class<T> implementation) {
        return install(Factory.find(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        return context.install(factory);
    }

    /** {@inheritDoc} */
    @Override
    public <T> SingletonConfiguration<T> installInstance(T instance) {
        return context.installInstance(instance);
    }

    /** {@inheritDoc} */
    @Override
    public StatelessConfiguration installStateless(Class<?> implementation) {
        return context.installStateless(implementation);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isArtifactRoot() {
        return context.hasParent();// not sure this is correct
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        context.lookup(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerConfiguration setDescription(String description) {
        context.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerConfiguration setName(String name) {
        context.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return context.use(extensionType);
    }
}
