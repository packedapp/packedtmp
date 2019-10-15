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
package packed.internal.service.build.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.service.Dependency;
import app.packed.service.InjectionException;
import app.packed.service.InstantiationMode;
import app.packed.service.PrototypeRequest;
import app.packed.service.Provide;
import app.packed.service.ServiceComponentConfiguration;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.run.LazyRuntimeEntry;
import packed.internal.service.run.PrototypeRuntimeEntry;
import packed.internal.service.run.RuntimeEntry;
import packed.internal.service.run.SingletonRuntimeEntry;
import packed.internal.util.ThrowableUtil;

/**
 * An entry representing a component node. This node is used for all three binding modes mainly because it makes
 * extending it with {@link ServiceComponentConfiguration} much easier.
 */
public final class ComponentBuildEntry<T> extends BuildEntry<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** The configuration of the component this build entry belongs to */
    public final ComponentConfiguration<?> componentConfiguration;

    /** The parent, if this node is the result of a member annotated with {@link Provide}. */
    private final ComponentBuildEntry<?> declaringEntry;

    boolean hasInstanceMembers;

    /** The singleton instance, not used for prototypes. */
    @Nullable
    private T instance;

    /** The instantiation mode of this node. */
    private InstantiationMode instantionMode;

    /** Is null for instance components. */
    @Nullable
    private MethodHandle mha;

    public ComponentBuildEntry(ConfigSite configSite, AtProvides atProvides, MethodHandle mh, ComponentBuildEntry<?> parent) {
        super(parent.serviceExtension, configSite, atProvides.dependencies);
        this.mha = requireNonNull(mh);
        // Rename to parentDependency??? and have it as null if instance
        this.declaringEntry = parent;
        this.instantionMode = atProvides.instantionMode;
        this.description = atProvides.description;
        this.componentConfiguration = parent.componentConfiguration;
    }

    public ComponentBuildEntry(ServiceExtensionNode injectorBuilder, ComponentConfiguration<T> cc, InstantiationMode instantionMode, MethodHandle mh,
            List<Dependency> dependencies) {
        super(injectorBuilder, cc.configSite(), dependencies);
        this.declaringEntry = null;
        this.instantionMode = requireNonNull(instantionMode);
        this.componentConfiguration = cc;
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
    public ComponentBuildEntry(ServiceExtensionNode ib, ConfigSite configSite, ComponentConfiguration<T> cc, T instance) {
        super(ib, configSite, List.of());
        this.instance = requireNonNull(instance, "instance is null");
        this.declaringEntry = null;
        this.instantionMode = InstantiationMode.SINGLETON;
        this.mha = null;
        this.componentConfiguration = requireNonNull(cc);
    }

    @Override
    @Nullable
    public BuildEntry<?> declaringEntry() {
        return declaringEntry;
    }

    /** {@inheritDoc} */
    @Override
    public final T getInstance(PrototypeRequest ignore) {
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
    public final boolean hasUnresolvedDependencies() {
        return !dependencies.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean requiresPrototypeRequest() {
        return hasDependencyOnInjectionSite;
    }

    private T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        int size = dependencies.size();
        if (size > 0) {
            params = new Object[size];
            for (int i = 0; i < resolvedDependencies.length; i++) {
                requireNonNull(resolvedDependencies[i]);
                params[i] = resolvedDependencies[i].getInstance(PrototypeRequest.of(dependencies.get(i)));
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

    /** {@inheritDoc} */
    @Override
    protected final RuntimeEntry<T> newRuntimeNode() {
        T i = instance;
        if (i != null) {
            return new SingletonRuntimeEntry<>(this, i);
        }
        if (instantionMode == InstantiationMode.PROTOTYPE) {
            return new PrototypeRuntimeEntry<>(this, toMethodHandle());
        } else {
            return new LazyRuntimeEntry<>(this, toMethodHandle(), null);
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

    private MethodHandle toMethodHandle() {
        MethodHandle mh = mha;
        if (declaringEntry != null && mh.type().parameterCount() != dependencies.size()) {
            mh = mha = mh.bindTo(declaringEntry.getInstance(null));
        }
        return mh;
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
