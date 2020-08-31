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

import app.packed.inject.ProvideContext;
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
public class PrototypeInjectorEntry<T> extends InjectorEntry<T> {

    /** An empty object array. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private final Provider<?>[] providers;

    private final MethodHandle mh;

    /**
     * @param node
     */
    public PrototypeInjectorEntry(ComponentFactoryBuildEntry<T> node, ServiceExtensionInstantiationContext context) {
        super(node);

        int size = node.resolvedDependencies.length;// .dependencies.size();
        providers = new Provider[size];
        for (int i = 0; i < node.resolvedDependencies.length; i++) {
            InjectorEntry<?> forReal = node.resolvedDependencies[i].toRuntimeEntry(context);
            ProvideContext is = null;
            if (node.offset >= i) {
                // System.out.println(node.offset + " " + node.dependencies.size());
                // PrototypeRequest.of(node.dependencies.get(node.offset + i));
            }
            providers[i] = () -> forReal.getInstance(is);
        }
        mh = node.mha;
        if (providers.length != mh.type().parameterCount()) {
            throw new Error(providers.length + "   " + mh.type().parameterCount());
        }
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.PROTOTYPE;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideContext site) {
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
