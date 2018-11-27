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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.inject.BindingMode;
import app.packed.inject.Factory;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.TypeLiteral;
import packed.internal.invokers.LookupDescriptorAccessor;
import packed.internal.util.AbstractConfiguration;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 *
 */
public abstract class AbstractInjectorBuilder extends AbstractConfiguration implements InjectorConfiguration {

    /** The lookup object. We default to public access */
    protected LookupDescriptorAccessor accessor = LookupDescriptorAccessor.PUBLIC;

    /**
     * Creates a new configuration.
     * 
     * @param configurationSite
     *            the configuration site
     * @param bundle
     *            if this configuration is created from a bundle
     */
    public AbstractInjectorBuilder(InternalConfigurationSite configurationSite) {
        super(configurationSite);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return bindFactory(BindingMode.SINGLETON, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return bindFactory(BindingMode.SINGLETON, factory);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return bindFactory(BindingMode.SINGLETON, Factory.findInjectable(implementation));
    }

    protected abstract <T> ServiceConfiguration<T> bindFactory(BindingMode mode, Factory<T> factory);

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return bindFactory(BindingMode.LAZY, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return bindFactory(BindingMode.LAZY, factory);
    }

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return bindFactory(BindingMode.LAZY, Factory.findInjectable(implementation));
    }

    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return bindFactory(BindingMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return bindFactory(BindingMode.PROTOTYPE, factory);
    }

    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        return bindFactory(BindingMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        checkConfigurable();
        this.accessor = LookupDescriptorAccessor.get(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractInjectorBuilder setDescription(String description) {
        super.setDescription(description);
        return this;
    }
}
