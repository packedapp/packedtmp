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
import java.util.List;

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
public final class ComponentConstantBuildEntry<T> extends AbstractComponentBuildEntry<T> {

    /**
     * Creates a new node from an instance.
     * 
     * @param ib
     *            the injector builder
     */
    public ComponentConstantBuildEntry(ServiceExtensionNode ib, ComponentNodeConfiguration cc) {
        super(ib, cc.configSite(), List.of(), null, cc, false);
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
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new IndexedEntry<>(this, context.region, component.source.singletonIndex);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected MethodHandle newMH(ServiceProvidingManager spm) {
        Object instance = component.source.instance();
        MethodHandle mh = MethodHandles.constant(instance.getClass(), component.source.instance());
        return MethodHandles.dropArguments(mh, 0, Region.class);
    }

    @Override
    public String toString() {
        return "Constant " + component.source.instance().getClass();
    }
}
