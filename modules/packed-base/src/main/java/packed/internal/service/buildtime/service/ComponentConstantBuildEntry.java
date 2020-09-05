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
import packed.internal.component.PackedWireableComponentDriver.SingletonComponentDriver;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.IndexedInjectorEntry;
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
        super(ib, cc.configSite(), List.of(), null, cc);
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
        T instance = instance();
        context.ns.storeSingleton(component, instance);
        return new IndexedInjectorEntry<>(this, context.ns, component.storeOffset);
    }

    @SuppressWarnings("unchecked")
    private T instance() {
        return ((SingletonComponentDriver<T>) component.driver()).instance;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected MethodHandle newMH(ServiceExtensionInstantiationContext context) {
        T instance = instance();
        return MethodHandles.constant(instance.getClass(), instance);
    }

    @Override
    public String toString() {
        return "Constant " + instance();
    }
}
