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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.container.ComponentConfiguration;
import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Provides;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.function.InternalFactoryExecutable;
import packed.internal.inject.function.InternalFactoryField;
import packed.internal.inject.function.InternalFactoryMember;
import packed.internal.inject.function.InternalFunction;
import packed.internal.inject.runtime.RuntimeServiceNode;
import packed.internal.inject.runtime.RuntimeServiceNodeLazy;
import packed.internal.inject.runtime.RuntimeServiceNodePrototype;
import packed.internal.inject.runtime.RuntimeServiceNodeSingleton;
import packed.internal.invokers.AccessibleExecutable;
import packed.internal.invokers.AccessibleField;
import packed.internal.invokers.ProvidesSupport.AtProvides;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;
import packed.internal.util.descriptor.InternalMethodDescriptor;

/**
 * A abstract node that builds thing from a factory. This node is used for all three binding modes mainly because it
 * makes extending it with {@link ComponentConfiguration} much easier.
 */
public class BuildNodeDefault<T> extends AbstractBuildNode<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** The binding mode of this node. */
    private final BindingMode bindingMode;

    /** An internal factory, null for nodes created from an instance. */
    @Nullable
    private InternalFactory<T> factory;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    private BuildNodeDefault<?> parent;

    BuildNodeDefault(InjectorBuilder injectorBuilder, InternalConfigurationSite configurationSite, BindingMode bindingMode, InternalFactory<T> factory,
            BuildNodeDefault<?> parent) {
        super(injectorBuilder, configurationSite, factory.dependencies);
        this.parent = parent;
        this.factory = requireNonNull(factory, "factory is null");
        this.bindingMode = requireNonNull(bindingMode);
        if (bindingMode != BindingMode.PROTOTYPE && hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
    }

    public BuildNodeDefault(InjectorBuilder injectorBuilder, InternalConfigurationSite configurationSite, BindingMode bindingMode, InternalFactory<T> factory) {
        super(injectorBuilder, configurationSite, factory.dependencies);
        this.factory = requireNonNull(factory, "factory is null");
        this.parent = null;
        this.bindingMode = requireNonNull(bindingMode);
        if (bindingMode != BindingMode.PROTOTYPE && hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
    }

    @Override
    AbstractBuildNode<?> declaringNode() {
        return parent;
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
    public BuildNodeDefault(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, T instance) {
        super(injectorConfiguration, configurationSite, List.of());
        this.instance = requireNonNull(instance, "instance is null");
        this.parent = null;
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

    private T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        int size = dependencies.size();
        if (size > 0) {
            params = new Object[size];
            for (int i = 0; i < resolvedDependencies.length; i++) {
                requireNonNull(resolvedDependencies[i]);
                params[i] = resolvedDependencies[i].getInstance(injectorBuilder.publicInjector, dependencies.get(i), null);
            }
        }

        T t = fac().instantiate(params);
        requireNonNull(t);
        return t;
    }

    private InternalFunction<T> fac() {
        if (parent != null) {
            InternalFactoryMember<T> ff = (InternalFactoryMember<T>) factory.function;
            if (ff.isMissingInstance()) {
                factory = ff.withInstance(parent.getInstance(null)).toFactory();
            }
        }
        return factory.function;
    }

    /** {@inheritDoc} */
    @Override
    final RuntimeServiceNode<T> newRuntimeNode() {
        T i = instance;
        if (i != null) {
            return new RuntimeServiceNodeSingleton<>(this, i, getBindingMode());
        }

        if (parent == null || parent.getBindingMode() == BindingMode.SINGLETON || parent.instance != null
                || (factory.function instanceof InternalFactoryMember && !((InternalFactoryMember<?>) factory.function).isMissingInstance())) {
            if (bindingMode == BindingMode.PROTOTYPE) {
                return new RuntimeServiceNodePrototype<>(this, fac());
            } else {
                return new RuntimeServiceNodeLazy<>(this, fac(), null);
            }
        }
        // parent==LAZY and not initialized, this.bindingMode=Lazy or Prototype

        return new RuntimeServiceNodeLazy<>(this, fac(), null);

    }

    public BuildNodeDefault<?> provide(AccessibleExecutable<AtProvides> s) {
        InternalMethodDescriptor m = (InternalMethodDescriptor) s.descriptor();
        InternalConfigurationSite icss = getConfigurationSite().spawnAnnotatedMethod(ConfigurationSiteType.INJECTOR_PROVIDE,
                s.metadata().getAnnotatedMember().getAnnotation(Provides.class), m);

        AtProvides atProvides = s.metadata();
        if (getBindingMode() == BindingMode.PROTOTYPE && !atProvides.isStaticMember()) {
            throw new InvalidDeclarationException("OOOPS");
        }
        Object instance = s.metadata().isStaticMember() ? null : this.instance;
        InternalFactoryExecutable<?> factory = new InternalFactoryExecutable<>(m.getReturnTypeLiteral(), m, s.metadata().getDependencies(), s.methodHandle(),
                instance);
        BuildNodeDefault<?> bnd = new BuildNodeDefault<>(injectorBuilder, icss, atProvides.getBindingMode(), factory.toFactory(), this);
        bnd.setDescription(atProvides.getDescription());
        return bnd;
    }

    public BuildNodeDefault<?> provide(AccessibleField<AtProvides> s) {
        InternalConfigurationSite icss = getConfigurationSite().spawnAnnotatedField(ConfigurationSiteType.INJECTOR_PROVIDE,
                s.metadata().getAnnotatedMember().getAnnotation(Provides.class), s.descriptor());

        AtProvides atProvides = s.metadata();
        if (getBindingMode() == BindingMode.PROTOTYPE && !atProvides.isStaticMember()) {
            throw new InvalidDeclarationException("OOOPS");
        }
        Object instance = s.metadata().isStaticMember() ? null : this.instance;
        InternalFactoryField<?> factory = new InternalFactoryField<>(s.descriptor().getTypeLiteral(), s.descriptor(), s.varHandle(), instance);
        BuildNodeDefault<?> bnd = new BuildNodeDefault<>(injectorBuilder, icss, atProvides.getBindingMode(), factory.toFactory(), this);
        bnd.setDescription(atProvides.getDescription());
        return bnd;
    }

    @Override
    public final String toString() {
        return factory == null ? String.valueOf(instance) : factory.toString();
    }
}
