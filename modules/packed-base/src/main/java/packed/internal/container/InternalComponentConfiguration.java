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

import java.util.ArrayList;
import java.util.HashMap;

import app.packed.container.ComponentConfiguration;
import app.packed.inject.BindingMode;
import app.packed.inject.Factory;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.util.Nullable;
import packed.internal.inject.buildnodes.BuildNodeDefault;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** The default implementation of {@link ComponentConfiguration}. */
public class InternalComponentConfiguration<T> extends BuildNodeDefault<T> implements ComponentConfiguration<T> {

    final @Nullable InternalComponentConfiguration<?> parent;

    /** A list of all children that have been added (lazily initialized). */
    ArrayList<InternalComponentConfiguration<?>> children;

    /** A map of all children that have been added whose name has been explicitly set (lazily initialized). */
    HashMap<String, InternalComponentConfiguration<?>> childrenExplicitNamed;

    /**
     * The thread that was used to create this configuration, is needed, because some operations are only allowed from the
     * installing thread.
     */
    final Thread initializationThread;

    /**
     * @param injectorConfiguration
     * @param configurationSite
     * @param factory
     * @param bindingMode
     */
    public InternalComponentConfiguration(InternalContainerConfiguration injectorConfiguration, InternalConfigurationSite configurationSite,
            @Nullable InternalComponentConfiguration<?> parent, InternalFactory<T> factory) {
        super(injectorConfiguration, configurationSite, factory, BindingMode.SINGLETON);
        this.parent = parent;
        this.initializationThread = Thread.currentThread();
    }

    /**
     * @param injectorConfiguration
     * @param configurationSite
     * @param instance
     */
    public InternalComponentConfiguration(InternalContainerConfiguration injectorConfiguration, InternalConfigurationSite configurationSite,
            @Nullable InternalComponentConfiguration<?> parent, T instance) {
        super(injectorConfiguration, configurationSite, instance);
        this.parent = parent;
        this.initializationThread = Thread.currentThread();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> addMixin(Class<?> implementation) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> addMixin(Factory<?> factory) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> addMixin(Object instance) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<T> as(Class<? super T> key) {
        super.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<T> as(Key<? super T> key) {
        super.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<?> asNone() {
        super.asNone();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getName() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <S> ComponentConfiguration<S> install(Class<S> implementation) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <S> ComponentConfiguration<S> install(Factory<S> factory) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <S> ComponentConfiguration<S> install(S instance) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InjectorConfiguration privates() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> setName(String name) {
        return null;
    }
}
