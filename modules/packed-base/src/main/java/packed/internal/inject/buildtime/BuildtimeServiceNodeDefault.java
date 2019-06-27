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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.InstantiationMode;
import app.packed.inject.Provide;
import app.packed.inject.ProvideHelper;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.annotations.AtProvides;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.config.site.ConfigSiteType;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.inject.InjectorBuilder;
import packed.internal.inject.InternalDependencyDescriptor;
import packed.internal.inject.runtime.RuntimeServiceNode;
import packed.internal.inject.runtime.RuntimeServiceNodeLazy;
import packed.internal.inject.runtime.RuntimeServiceNodePrototype;
import packed.internal.inject.runtime.RuntimeServiceNodeSingleton;
import packed.internal.invokable.InternalFunction;
import packed.internal.invokable.InvokableMember;

/**
 * A abstract node that builds thing from a factory. This node is used for all three binding modes mainly because it
 * makes extending it with {@link ProvidedComponentConfiguration} much easier.
 */
public class BuildtimeServiceNodeDefault<T> extends BuildtimeServiceNode<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    final ServiceClassDescriptor descriptor;

    /** An internal factory, null for nodes created from an instance. */
    @Nullable
    private InternalFunction<T> function;

    public boolean hasInstanceMembers;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    /** The instantiation mode of this node. */
    private InstantiationMode instantionMode;

    /** The parent, if this node is the result of a member annotated with {@link Provide}. */
    private final BuildtimeServiceNodeDefault<?> parent;

    public BuildtimeServiceNodeDefault(InjectorBuilder injectorBuilder, InternalConfigSite configSite, ServiceClassDescriptor descriptor,
            InstantiationMode instantionMode, InternalFunction<T> function, List<InternalDependencyDescriptor> dependencies) {
        super(injectorBuilder, configSite, dependencies);
        this.function = requireNonNull(function, "factory is null");
        this.parent = null;
        this.descriptor = requireNonNull(descriptor);
        this.instantionMode = requireNonNull(instantionMode);

        // Maaske skal vi bare smide UnsupportedOperationException istedet for???
        // Vi faar jo problemet ved f.eks. CACHE_PER_APP.....
        // her giver det ikke meningen at faa componenten...

        // if (instantionMode != InstantiationMode.PROTOTYPE && hasDependencyOnInjectionSite) {
        // throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        // }
    }

    /**
     * Creates a new node from an instance.
     * 
     * @param injectorConfiguration
     *            the injector configuration
     * @param configSite
     *            the configuration site
     * @param instance
     *            the instance
     */
    public BuildtimeServiceNodeDefault(InjectorBuilder injectorConfiguration, InternalConfigSite configSite, ServiceClassDescriptor descriptor,
            T instance) {
        super(injectorConfiguration, configSite, List.of());
        this.instance = requireNonNull(instance, "instance is null");
        this.descriptor = requireNonNull(descriptor);
        this.parent = null;
        this.instantionMode = InstantiationMode.SINGLETON;
        this.function = null;
    }

    BuildtimeServiceNodeDefault(InternalConfigSite configSite, AtProvides atProvides, InternalFunction<T> factory,
            BuildtimeServiceNodeDefault<?> parent) {
        super(parent.injectorBuilder, configSite, atProvides.dependencies);
        this.parent = parent;
        this.function = requireNonNull(factory, "factory is null");
        this.instantionMode = atProvides.instantionMode;
        this.descriptor = null;
        description = atProvides.description;
    }

    @Override
    BuildtimeServiceNode<?> declaringNode() {
        return parent;
    }

    protected ServiceClassDescriptor descriptor() {
        return descriptor;
    }

    private InternalFunction<T> fac() {
        if (parent != null) {
            InvokableMember<T> ff = (InvokableMember<T>) function;
            if (ff.isMissingInstance()) {
                function = ff.withInstance(parent.getInstance(null));
            }
        }
        return function;
    }

    /** {@inheritDoc} */
    @Override
    public final T getInstance(ProvideHelper ignore) {
        if (instantionMode == InstantiationMode.PROTOTYPE) {
            return newInstance();
        }

        T i = instance;
        if (i == null) {
            instance = i = newInstance();
        }
        return i;
    }

    public BuildtimeServiceNodeDefault<T> instantiateAs(InstantiationMode mode) {
        requireNonNull(mode, "mode is null");
        this.instantionMode = mode;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final InstantiationMode instantiationMode() {
        return instantionMode;
    }

    public void lazy() {
        instantiateAs(InstantiationMode.LAZY);
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
                params[i] = resolvedDependencies[i].getInstance(injectorBuilder == null ? null : injectorBuilder.publicInjector, dependencies.get(i), null);
            }
        }

        T t = fac().invoke(params);
        requireNonNull(t);
        return t;
    }

    /** {@inheritDoc} */
    @Override
    final RuntimeServiceNode<T> newRuntimeNode() {
        T i = instance;
        if (i != null) {
            return new RuntimeServiceNodeSingleton<>(this, i);
        }

        if (parent == null || parent.instantiationMode() == InstantiationMode.SINGLETON || parent.instance != null
                || (function instanceof InvokableMember && !((InvokableMember<?>) function).isMissingInstance())) {
            if (instantionMode == InstantiationMode.PROTOTYPE) {
                return new RuntimeServiceNodePrototype<>(this, fac());
            } else {
                return new RuntimeServiceNodeLazy<>(this, fac(), null);
            }
        }
        // parent==LAZY and not initialized, this.instantionMode=Lazy or Prototype

        return new RuntimeServiceNodeLazy<>(this, fac(), null);

    }

    public void prototype() {
        if (hasDependencyOnInjectionSite) {
            throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        }
        if (hasInstanceMembers) {
            throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as prototypes");
        }
        instantiateAs(InstantiationMode.PROTOTYPE);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BuildtimeServiceNode<?> provide(AtProvides atProvides) {
        InternalConfigSite icss = configSite().thenAnnotatedMember(ConfigSiteType.INJECTOR_PROVIDE, atProvides.provides,
                atProvides.member);

        InvokableMember<?> fi = atProvides.invokable;
        if (!atProvides.isStaticMember) {
            // getInstance(null);
            // fi = fi.withInstance(this.instance);
        }

        BuildtimeServiceNodeDefault<?> node = new BuildtimeServiceNodeDefault<>(icss, atProvides, fi, this);
        node.as((Key) atProvides.key);
        return node;
    }

    @Override
    public final String toString() {
        return function == null ? String.valueOf(instance) : function.toString();
    }
}
