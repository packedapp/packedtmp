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
import packed.internal.inject.InternalDependency;
import packed.internal.inject.function.FieldInvoker;
import packed.internal.inject.function.InternalFactoryExecutable;
import packed.internal.inject.function.InternalFactoryMember;
import packed.internal.inject.function.InternalFunction;
import packed.internal.inject.runtime.RuntimeServiceNode;
import packed.internal.inject.runtime.RuntimeServiceNodeLazy;
import packed.internal.inject.runtime.RuntimeServiceNodePrototype;
import packed.internal.inject.runtime.RuntimeServiceNodeSingleton;
import packed.internal.inject.support.AccessibleExecutable;
import packed.internal.inject.support.AtProvides;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;
import packed.internal.util.descriptor.InternalFieldDescriptor;
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
    private InternalFunction<T> function;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    /** The parent, if this node is the result of a member annotated with {@link Provides}. */
    private final BuildNodeDefault<?> parent;

    BuildNodeDefault(InternalConfigurationSite configurationSite, AtProvides atProvides, InternalFunction<T> factory, BuildNodeDefault<?> parent) {
        super(parent.injectorBuilder, configurationSite, atProvides.dependencies);
        this.parent = parent;
        this.function = requireNonNull(factory, "factory is null");
        this.bindingMode = atProvides.bindingMode;
        setDescription(atProvides.description);
    }

    public BuildNodeDefault(InjectorBuilder injectorBuilder, InternalConfigurationSite configurationSite, BindingMode bindingMode, InternalFunction<T> factory,
            List<InternalDependency> dependencies) {
        super(injectorBuilder, configurationSite, dependencies);
        this.function = requireNonNull(factory, "factory is null");
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
        this.function = null;
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

        T t = fac().invoke(params);
        requireNonNull(t);
        return t;
    }

    private InternalFunction<T> fac() {
        if (parent != null) {
            InternalFactoryMember<T> ff = (InternalFactoryMember<T>) function;
            if (ff.isMissingInstance()) {
                function = ff.withInstance(parent.getInstance(null));
            }
        }
        return function;
    }

    /** {@inheritDoc} */
    @Override
    final RuntimeServiceNode<T> newRuntimeNode() {
        T i = instance;
        if (i != null) {
            return new RuntimeServiceNodeSingleton<>(this, i, getBindingMode());
        }

        if (parent == null || parent.getBindingMode() == BindingMode.SINGLETON || parent.instance != null
                || (function instanceof InternalFactoryMember && !((InternalFactoryMember<?>) function).isMissingInstance())) {
            if (bindingMode == BindingMode.PROTOTYPE) {
                return new RuntimeServiceNodePrototype<>(this, fac());
            } else {
                return new RuntimeServiceNodeLazy<>(this, fac(), null);
            }
        }
        // parent==LAZY and not initialized, this.bindingMode=Lazy or Prototype

        return new RuntimeServiceNodeLazy<>(this, fac(), null);

    }

    public AbstractBuildNode<?> provideMethod(AtProvides atProvides) {
        InternalMethodDescriptor m = (InternalMethodDescriptor) atProvides.annotatedMember;
        InternalConfigurationSite icss = getConfigurationSite().spawnAnnotatedMethod(ConfigurationSiteType.INJECTOR_PROVIDE,
                atProvides.annotatedMember.getAnnotation(Provides.class), m);

        Object instance = atProvides.isStaticMember ? null : this.instance;
        return new BuildNodeDefault<>(icss, atProvides,
                new InternalFactoryExecutable<>(m.getReturnTypeLiteral(), m, ((AccessibleExecutable) atProvides.am).methodHandle(), instance), this);
    }

    public AbstractBuildNode<?> provideField(AtProvides atProvides) {
        InternalFieldDescriptor descriptor = (InternalFieldDescriptor) atProvides.annotatedMember;
        InternalConfigurationSite icss = getConfigurationSite().spawnAnnotatedField(ConfigurationSiteType.INJECTOR_PROVIDE,
                atProvides.annotatedMember.getAnnotation(Provides.class), descriptor);

        FieldInvoker<?> fi = ((FieldInvoker<?>) atProvides.ifm);
        if (!atProvides.isStaticMember) {
            getInstance(null);
            fi = fi.withInstance(this.instance);
        }
        return new BuildNodeDefault<>(icss, atProvides, fi, this);
    }

    @Override
    public final String toString() {
        return function == null ? String.valueOf(instance) : function.toString();
    }
}
