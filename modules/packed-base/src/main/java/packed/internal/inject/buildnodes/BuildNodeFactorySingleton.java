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

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.inject.runtimenodes.RuntimeNodeFactoryLazy;
import packed.internal.inject.runtimenodes.RuntimeNodeInstance;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** A node that creates a single (possible lazy) instance of a service. */
public final class BuildNodeFactorySingleton<T> extends BuildNodeFactory<T> {

    /** Whether or not the node is lazy */
    private final BindingMode bindingMode;

    /** The singleton instance. */
    @Nullable
    private T instance;

    /**
     * Creates a new node.
     * 
     * @param injectorConfiguration
     *            the injector configuration
     * @param configurationSite
     *            the configuration site of this node
     * @param factory
     *            the factory that the singleton will be created from
     * @param isLazy
     *            whether or not the instance is lazily created
     */
    public BuildNodeFactorySingleton(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite,
            InternalFactory<T> factory, boolean isLazy) {
        super(injectorConfiguration, configurationSite, factory);
        if (hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
        this.bindingMode = isLazy ? BindingMode.LAZY_SINGLETON : BindingMode.EAGER_SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return bindingMode;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite ignore) {
        T i = instance;
        if (i == null) {
            instance = i = newInstance();
        }
        return i;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    RuntimeNode<T> newRuntimeNode() {
        T i = instance;
        if (i == null && bindingMode == BindingMode.LAZY_SINGLETON) {
            return new RuntimeNodeFactoryLazy<>(this, factory);
        } else {
            return new RuntimeNodeInstance<>(this, i, getBindingMode());
        }
    }
}
