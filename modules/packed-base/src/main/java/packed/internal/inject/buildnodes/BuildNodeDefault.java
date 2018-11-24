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

import app.packed.container.ComponentConfiguration;
import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.factory.InternalFactoryField;
import packed.internal.inject.runtimenodes.RuntimeServiceNode;
import packed.internal.inject.runtimenodes.RuntimeServiceNodeLazy;
import packed.internal.inject.runtimenodes.RuntimeServiceNodePrototype;
import packed.internal.inject.runtimenodes.RuntimeServiceNodeSingleton;
import packed.internal.invokers.AccessibleField;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;
import packed.internal.util.descriptor.AtProvides;

/**
 * A abstract node that builds thing from a factory. This node is used for all three binding modes mainly because it
 * makes extending it with {@link ComponentConfiguration} much easier.
 */
public class BuildNodeDefault<T> extends BuildNode<T> {

    /** The binding mode of this node. */
    private final BindingMode bindingMode;

    /** An empty object array. */
    private final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** The internal factory. */
    @Nullable
    final InternalFactory<T> factory;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    public BuildNodeDefault(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, InternalFactory<T> factory,
            BindingMode bindingMode) {
        super(injectorConfiguration, configurationSite, factory.getDependencies());
        this.factory = requireNonNull(factory, "factory is null");
        this.bindingMode = requireNonNull(bindingMode);
        if (bindingMode != BindingMode.PROTOTYPE && hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
    }

    public BuildNodeDefault<?> provide(AccessibleField<AtProvides> s) {
        InternalConfigurationSite icss = getConfigurationSite().spawnAnnotatedField(ConfigurationSiteType.INJECTOR_PROVIDE,
                s.metadata().getAnnotatedMember().getAnnotation(Provides.class), s.descriptor());

        AtProvides atProvides = s.metadata();
        InternalFactoryField<?> factory = new InternalFactoryField<>(s.descriptor().getTypeLiteral(), s.descriptor(), s.varHandle(), instance);
        return new BuildNodeDefault<>(injectorConfiguration, icss, factory, atProvides.getBindingMode());
    }

    /**
     * Creates a new node from an instance.
     * 
     * @param injectorConfiguration
     *            the injector configuration
     * @param configurationSite
     *            the configuration site
     * @param instance
     *            the instance
     */
    public BuildNodeDefault(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, T instance) {
        super(injectorConfiguration, configurationSite, List.of());
        this.instance = requireNonNull(instance, "instance is null");
        this.bindingMode = BindingMode.SINGLETON;
        this.factory = null;
    }

    /** {@inheritDoc} */
    @Override
    public final BindingMode getBindingMode() {
        return bindingMode;
    }

    /** {@inheritDoc} */
    @Override
    public final T getInstance(InjectionSite ignore) {
        if (bindingMode == BindingMode.PROTOTYPE) {
            return newInstance();
        }

        T i = instance;
        if (i == null) {
            instance = i = newInstance();
        }
        return i;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean needsInjectionSite() {
        return hasDependencyOnInjectionSite;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean needsResolving() {
        return !dependencies.isEmpty();
    }

    final T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        int size = dependencies.size();
        if (size > 0) {
            params = new Object[size];
            for (int i = 0; i < resolvedDependencies.length; i++) {
                requireNonNull(resolvedDependencies[i]);
                params[i] = resolvedDependencies[i].getInstance(injectorConfiguration.publicInjector, dependencies.get(i), null);
            }
        }
        return factory.instantiate(params);
    }

    /** {@inheritDoc} */
    @Override
    final RuntimeServiceNode<T> newRuntimeNode() {
        if (bindingMode == BindingMode.PROTOTYPE) {
            return new RuntimeServiceNodePrototype<>(this, factory);
        }
        T i = instance;
        if (i == null && bindingMode == BindingMode.LAZY) {
            return new RuntimeServiceNodeLazy<>(this, factory);
        } else {
            return new RuntimeServiceNodeSingleton<>(this, i, getBindingMode());
        }
    }

    @Override
    public final String toString() {
        return factory == null ? String.valueOf(instance) : factory.toString();
    }
}
