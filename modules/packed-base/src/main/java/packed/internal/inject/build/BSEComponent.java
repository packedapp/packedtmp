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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.inject.InjectionException;
import app.packed.inject.InstantiationMode;
import app.packed.inject.Provide;
import app.packed.inject.ProvideHelper;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.InjectorConfigSiteOperations;
import packed.internal.inject.run.RSE;
import packed.internal.inject.run.RSESingleton;
import packed.internal.inject.run.RSNLazy;
import packed.internal.inject.run.RSNPrototype;
import packed.internal.inject.util.AtProvides;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.invoke.FunctionHandle;
import packed.internal.invoke.InvokableMember;
import packed.internal.util.ThrowableUtil;

/**
 * An entry representing a component node. This node is used for all three binding modes mainly because it makes
 * extending it with {@link ProvidedComponentConfiguration} much easier.
 */
public final class BSEComponent<T> extends BSE<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** An internal factory, null for nodes created from an instance. */
    @Nullable
    private FunctionHandle<T> function;

    public boolean hasInstanceMembers;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    /** The instantiation mode of this node. */
    private InstantiationMode instantionMode;

    /** The parent, if this node is the result of a member annotated with {@link Provide}. */
    private final BSEComponent<?> receiver;

    public BSEComponent(InjectorBuilder injectorBuilder, ComponentConfiguration cc, InstantiationMode instantionMode, FunctionHandle<T> function,
            List<InternalDependencyDescriptor> dependencies) {
        super(injectorBuilder, cc.configSite(), dependencies);
        this.function = requireNonNull(function, "factory is null");
        this.receiver = null;
        this.instantionMode = requireNonNull(instantionMode);

        // Maaske skal vi bare smide UnsupportedOperationException istedet for???
        // Vi faar jo problemet ved f.eks. CACHE_PER_APP.....
        // her giver det ikke meningen at faa componenten...

        // if (instantionMode != InstantiationMode.PROTOTYPE && hasDependencyOnInjectionSite) {
        // throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        // }
        mh = null;
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
    public BSEComponent(InjectorBuilder injectorConfiguration, ConfigSite configSite, T instance) {
        super(injectorConfiguration, configSite, List.of());
        this.instance = requireNonNull(instance, "instance is null");
        this.receiver = null;
        this.instantionMode = InstantiationMode.SINGLETON;
        this.function = null;
        this.mh = null;
    }

    private final MethodHandle mh;

    BSEComponent(ConfigSite configSite, AtProvides atProvides, MethodHandle mh, FunctionHandle<T> factory, BSEComponent<?> parent) {
        super(parent.injectorBuilder, configSite, atProvides.dependencies);
        this.mh = requireNonNull(mh);
        this.receiver = parent;
        this.function = requireNonNull(factory, "factory is null");
        this.instantionMode = atProvides.instantionMode;
        this.description = atProvides.description;
    }

    @Override
    public BSE<?> declaringNode() {
        return receiver;
    }

    private FunctionHandle<T> fac() {
        if (receiver != null) {
            InvokableMember<T> ff = (InvokableMember<T>) function;
            if (ff.isMissingInstance()) {
                function = ff.withInstance(receiver.getInstance(null));
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

    public BSEComponent<T> instantiateAs(InstantiationMode mode) {
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
                params[i] = resolvedDependencies[i].getInstance(injectorBuilder == null ? null : injectorBuilder.resolver.publicInjector, dependencies.get(i),
                        null);
            }
        }
        Object o;
        MethodHandle mh = fac().toMethodHandle();

        // It would actually be nice with the receiver here as well...
        try {
            o = mh.invokeWithArguments(params);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new InjectionException("foo", e);
        }
        @SuppressWarnings("unchecked")
        T t = (T) o;
        requireNonNull(t);
        return t;
    }

    /** {@inheritDoc} */
    @Override
    final RSE<T> newRuntimeNode() {
        T i = instance;
        if (i != null) {
            return new RSESingleton<>(this, i);
        }

        if (receiver == null || receiver.instantiationMode() == InstantiationMode.SINGLETON || receiver.instance != null
                || (function instanceof InvokableMember && !((InvokableMember<?>) function).isMissingInstance())) {
            if (instantionMode == InstantiationMode.PROTOTYPE) {
                return new RSNPrototype<>(this, fac());
            } else {
                return new RSNLazy<>(this, fac(), null);
            }
        }
        // parent==LAZY and not initialized, this.instantionMode=Lazy or Prototype

        return new RSNLazy<>(this, fac(), null);

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
    public BSE<?> provide(AtProvides atProvides) {
        ConfigSite icss = configSite().thenAnnotatedMember(InjectorConfigSiteOperations.INJECTOR_PROVIDE, atProvides.provides, atProvides.member);

        InvokableMember<?> im = atProvides.invokable;
        // if (!atProvides.isStaticMember) {
        // // getInstance(null);
        // // fi = fi.withInstance(this.instance);
        // }
        BSEComponent<?> node = new BSEComponent<>(icss, atProvides, atProvides.methodHandle, im, this);
        node.as((Key) atProvides.key);
        return node;
    }

    @Override
    public final String toString() {
        return function == null ? String.valueOf(instance) : function.toString();
    }
}
