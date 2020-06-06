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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.component.ComponentDescriptor;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;

/**
 *
 */
public final class PackedSingletonConfiguration<T> extends AbstractComponentConfiguration implements SingletonConfiguration<T> {

    public final ComponentModel componentModel;

    @Nullable
    public final BaseFactory<T> factory;

    @Nullable
    public final T instance;

    public PackedSingletonConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent, ComponentModel componentModel, BaseFactory<T> factory) {
        super(configSite, parent);
        this.componentModel = requireNonNull(componentModel);
        this.factory = requireNonNull(factory);
        this.instance = null;
    }

    public PackedSingletonConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent, ComponentModel componentModel, T instance) {
        super(configSite, parent);
        this.componentModel = requireNonNull(componentModel);
        this.factory = null;
        this.instance = requireNonNull(instance);
    }

    public MethodHandle fromFactory() {
        FactoryHandle<?> handle = factory.factory.handle;
        return container().fromFactoryHandle(handle);
    }

    /** {@inheritDoc} */
    @Override
    protected String initializeNameDefaultName() {
        return componentModel.defaultPrefix();
    }

    /** {@inheritDoc} */
    @Override
    public AbstractComponent instantiate(AbstractComponent parent, PackedInstantiationContext paic) {
        return new AbstractComponent(parent, this, paic, ComponentDescriptor.COMPONENT_INSTANCE);
    }

    public PackedSingletonConfiguration<T> runHooks(Object source) {
        componentModel.invokeOnHookOnInstall(source, this);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedSingletonConfiguration<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedSingletonConfiguration<T> setName(String name) {
        super.setName(name);
        return this;
    }
}
