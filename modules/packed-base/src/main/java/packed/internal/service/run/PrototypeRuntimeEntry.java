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
package packed.internal.service.run;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.service.InjectionException;
import app.packed.service.InstantiationMode;
import app.packed.service.PrototypeRequest;
import packed.internal.inject.util.Provider;
import packed.internal.service.build.BuildEntry;
import packed.internal.util.ThrowableUtil;

/** A runtime service node for prototypes. */
// 3 typer?? Saa kan de foerste to implementere Provider
// No params
// No InjectionSite parameters
// InjectionSite parameters
public final class PrototypeRuntimeEntry<T> extends RuntimeEntry<T> implements Provider<T> {

    /** An empty object array. */
    private final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private final Provider<?>[] providers;

    private final MethodHandle mh;

    /**
     * @param node
     */
    public PrototypeRuntimeEntry(BuildEntry<T> node, MethodHandle mh) {
        super(node);
        this.mh = requireNonNull(mh);
        this.providers = new Provider[node.dependencies.size()];
        for (int i = 0; i < providers.length; i++) {
            RuntimeEntry<?> forReal = node.resolvedDependencies[i].toRuntimeEntry();
            PrototypeRequest is = null;
            PrototypeRequest.of(node.dependencies.get(i));
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
    public T getInstance(PrototypeRequest site) {
        return newInstance();
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }

    /**
     * Creates a new service instance.
     *
     * @return the new service instance
     */
    @SuppressWarnings("unchecked")
    private T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        if (providers.length > 0) {
            params = new Object[providers.length];
            for (int i = 0; i < providers.length; i++) {
                params[i] = providers[i].get();
            }
        }
        try {
            return (T) mh.invokeWithArguments(params);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new InjectionException("Failed to inject ", e);
        }
    }
}
