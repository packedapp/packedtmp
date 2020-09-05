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
package packed.internal.service.runtime;

import java.lang.invoke.MethodHandle;

import app.packed.inject.ProvidePrototypeContext;
import app.packed.inject.ProvisionException;
import packed.internal.service.buildtime.Provider;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.buildtime.service.ComponentFactoryBuildEntry;
import packed.internal.util.ThrowableUtil;

/** A runtime service node for prototypes. */
// 3 typer?? Saa kan de foerste to implementere Provider
// No params
// No InjectionSite parameters
// InjectionSite parameters
public class Prototype2InjectorEntry<T> extends RuntimeEntry<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private final Provider<?>[] providers;

    private final MethodHandle mh;

    private final T instance;

    /**
     * @param node
     */
    public Prototype2InjectorEntry(ComponentFactoryBuildEntry<T> node, ServiceExtensionInstantiationContext context) {
        super(node);
        int size = node.resolvedDependencies.length;
        providers = new Provider[size];
        for (int i = 0; i < node.resolvedDependencies.length; i++) {
            RuntimeEntry<?> forReal = node.resolvedDependencies[i].toRuntimeEntry(context);
            providers[i] = () -> forReal.getInstance(null);
        }
        mh = node.mha;
        this.instance = newInstance();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.CONSTANT;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvidePrototypeContext site) {
        return instance;
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
    final T newInstance() {
        Object[] params = EMPTY_OBJECT_ARRAY;
        if (providers.length > 0) {
            params = new Object[providers.length];
            for (int i = 0; i < providers.length; i++) {
                params[i] = providers[i].provide();
            }
        }
        try {
            return (T) mh.invokeWithArguments(params);
        } catch (Throwable e) {
            ThrowableUtil.throwIfUnchecked(e);
            throw new ProvisionException("Failed to inject ", e);
        }
    }
}
