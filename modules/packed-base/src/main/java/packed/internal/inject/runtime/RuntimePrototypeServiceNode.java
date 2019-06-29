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
package packed.internal.inject.runtime;

import static java.util.Objects.requireNonNull;

import app.packed.inject.ProvideHelper;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import packed.internal.inject.Provider;
import packed.internal.inject.buildtime.BuildtimeServiceNode;
import packed.internal.invokable.InternalFunction;

/** A runtime service node for prototypes. */
// 3 typer?? Saa kan de foerste to implementere Provider
// No params
// No InjectionSite parameters
// InjectionSite parameters
public final class RuntimePrototypeServiceNode<T> extends AbstractRuntimeServiceNode<T> implements Provider<T> {

    /** An empty object array. */
    private final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** The factory used for creating new instances. */
    private final InternalFunction<T> invoker;

    Provider<?>[] providers;

    /**
     * @param node
     */
    public RuntimePrototypeServiceNode(BuildtimeServiceNode<T> node, InternalFunction<T> function) {
        super(node);
        this.invoker = requireNonNull(function);
        this.providers = new Provider[node.dependencies.size()];
        for (int i = 0; i < providers.length; i++) {
            AbstractRuntimeServiceNode<?> forReal = node.resolvedDependencies[i].toRuntimeNode();
            ProvideHelper is = null;
            ProvideHelper.of(Injector.configure(c -> {}), node.dependencies.get(i));
            providers[i] = () -> forReal.getInstance(is);
        }
        // Create local injection site for each parameter.
        // Wrap them in Function<InjectionSite, O>
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return newInstance();
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return InstantiationMode.PROTOTYPE;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideHelper site) {
        return newInstance();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /**
     * Creates a new service instance.
     *
     * @return the new service instance
     */
    private T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        if (providers.length > 0) {
            params = new Object[providers.length];
            for (int i = 0; i < providers.length; i++) {
                params[i] = providers[i].get();
            }
        }
        return invoker.invoke(params);
    }
}
