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
import packed.internal.component.Region;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.buildtime.service.ComponentMethodHandleBuildEntry;
import packed.internal.util.ThrowableUtil;

/** A runtime service node for prototypes. */
// 3 typer?? Saa kan de foerste to implementere Provider
// No params
// No InjectionSite parameters
// InjectionSite parameters
public class PrototypeInjectorEntry<T> extends RuntimeEntry<T> {

    private final MethodHandle mh;

    // Bliver nok noedt til at gemme en component...
    // Men det kan vi vel bare goere
    private final Region ns;

    /**
     * @param node
     */
    public PrototypeInjectorEntry(ComponentMethodHandleBuildEntry<T> node, ServiceExtensionInstantiationContext context) {
        super(node);
        this.ns = context.region;
        this.mh = node.source.reducedMha;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.PROTOTYPE;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvidePrototypeContext site) {
        try {
            return (T) mh.invoke(ns);
        } catch (Throwable e) {
            ThrowableUtil.throwIfUnchecked(e);
            throw new ProvisionException("Failed to inject ", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
