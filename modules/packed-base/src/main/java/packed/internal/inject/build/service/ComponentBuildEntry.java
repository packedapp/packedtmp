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
package packed.internal.inject.build.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.service.ComponentServiceConfiguration;
import app.packed.service.InjectionException;
import app.packed.service.InstantiationMode;
import app.packed.service.Provide;
import app.packed.service.ServiceDependency;
import app.packed.service.ServiceRequest;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.InjectionExtensionNode;
import packed.internal.inject.run.RSE;
import packed.internal.inject.run.RSESingleton;
import packed.internal.inject.run.RSNLazy;
import packed.internal.inject.run.RSNPrototype;
import packed.internal.util.ThrowableUtil;

/**
 * An entry representing a component node. This node is used for all three binding modes mainly because it makes
 * extending it with {@link ComponentServiceConfiguration} much easier.
 */
public final class ComponentBuildEntry<T> extends BuildEntry<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    boolean hasInstanceMembers;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    /** The instantiation mode of this node. */
    private InstantiationMode instantionMode;

    /** Is null for instance components. */
    @Nullable
    private MethodHandle mha;

    /** The parent, if this node is the result of a member annotated with {@link Provide}. */
    private final ComponentBuildEntry<?> parent;

    public ComponentBuildEntry(ConfigSite configSite, AtProvides atProvides, MethodHandle mh, ComponentBuildEntry<?> parent) {
        super(parent.injectorBuilder, configSite, atProvides.dependencies);
        this.mha = requireNonNull(mh);
        this.parent = parent;
        this.instantionMode = atProvides.instantionMode;
        this.description = atProvides.description;
    }

    public ComponentBuildEntry(InjectionExtensionNode injectorBuilder, ComponentConfiguration cc, InstantiationMode instantionMode, MethodHandle mh,
            List<ServiceDependency> dependencies) {
        super(injectorBuilder, cc.configSite(), dependencies);
        this.parent = null;
        this.instantionMode = requireNonNull(instantionMode);

        // Maaske skal vi bare smide UnsupportedOperationException istedet for???
        // Vi faar jo problemet ved f.eks. CACHE_PER_APP.....
        // her giver det ikke meningen at faa componenten...

        // if (instantionMode != InstantiationMode.PROTOTYPE && hasDependencyOnInjectionSite) {
        // throw new InvalidDeclarationException("Cannot inject InjectionSite into singleton services");
        // }
        mha = requireNonNull(mh);
    }

    /**
     * Creates a new node from an instance.
     * 
     * @param ib
     *            the injector builder
     * @param configSite
     *            the configuration site
     * @param instance
     *            the instance
     */
    public ComponentBuildEntry(InjectionExtensionNode ib, ConfigSite configSite, T instance) {
        super(ib, configSite, List.of());
        this.instance = requireNonNull(instance, "instance is null");
        this.parent = null;
        this.instantionMode = InstantiationMode.SINGLETON;
        this.mha = null;
    }

    @Override
    public BuildEntry<?> declaringNode() {
        return parent;
    }

    /** {@inheritDoc} */
    @Override
    public final T getInstance(ServiceRequest ignore) {
        if (instantionMode == InstantiationMode.PROTOTYPE) {
            return newInstance();
        }

        T i = instance;
        if (i == null) {
            instance = i = newInstance();
        }
        return i;
    }

    public ComponentBuildEntry<T> instantiateAs(InstantiationMode mode) {
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
                params[i] = resolvedDependencies[i].getInstance(dependencies.get(i), null);
            }
        }
        Object o;
        MethodHandle mh = toMethodHandle();

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

    private MethodHandle toMethodHandle() {
        MethodHandle mh = mha;
        if (parent != null && mh.type().parameterCount() != dependencies.size()) {
            mh = mha = mh.bindTo(parent.getInstance(null));
        }
        return mh;
    }

    /** {@inheritDoc} */
    @Override
    protected final RSE<T> newRuntimeNode() {
        T i = instance;
        if (i != null) {
            return new RSESingleton<>(this, i);
        }
        if (instantionMode == InstantiationMode.PROTOTYPE) {
            return new RSNPrototype<>(this, toMethodHandle());
        } else {
            return new RSNLazy<>(this, toMethodHandle(), null);
        }
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
}
// if (parent == null || parent.instantiationMode() == InstantiationMode.SINGLETON || parent.instance != null
// || (function instanceof InvokableMember && !((InvokableMember<?>) function).isMissingInstance())) {

// }
// // parent==LAZY and not initialized, this.instantionMode=Lazy or Prototype
// if (true) {
// throw new Error();
// }
// return new RSNLazy<>(this, toMethodHandle(), null);
