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

import java.util.List;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Provider;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.inject.runtimenodes.RuntimeNodeInstance;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** A build node holding an instance. */
public class BuildNodeInstance<T> extends BuildNode<T> implements Provider<T> {

    /** The instance. */
    private final T instance;

    /**
     * Creates a new instance holding build node.
     *
     * @param injectorConfiguration
     *            the injector configuration this node is being added to
     * @param configurationSite
     *            the configuration site of the node
     * @param instance
     *            the instance
     */
    public BuildNodeInstance(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, T instance) {
        super(injectorConfiguration, configurationSite, List.of());
        this.instance = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return BindingMode.SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    RuntimeNode<T> newRuntimeNode() {
        return new RuntimeNodeInstance<>(this, instance, getBindingMode());
    }
}
