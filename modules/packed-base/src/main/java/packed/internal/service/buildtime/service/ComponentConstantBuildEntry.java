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
package packed.internal.service.buildtime.service;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.Region;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.IndexedEntry;
import packed.internal.service.runtime.RuntimeEntry;

/**
 *
 */
public final class ComponentConstantBuildEntry<T> extends ComponentBuildEntry<T> {

    /**
     * Creates a new node from an instance.
     * 
     * @param services
     *            the injector builder
     */
    public ComponentConstantBuildEntry(ServiceExtensionNode services, ComponentNodeConfiguration component) {
        super(services, component.configSite(), component, null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.CONSTANT;
    }

    /** {@inheritDoc} */
    @Override
    protected MethodHandle newMH(ServiceProvidingManager spm) {
        // Taenker vi hellere vil laese fra arrayet...
        // Paa lang sigt vil vi gerne cache de method handles vi generere...
        Object instance = component.source.instance();
        MethodHandle mh = MethodHandles.constant(instance.getClass(), component.source.instance());
        return MethodHandles.dropArguments(mh, 0, Region.class);
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new IndexedEntry<>(this, context.region, component.source.singletonIndex);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }

    @Override
    public String toString() {
        return "Constant " + component.source.instance().getClass();
    }
}
