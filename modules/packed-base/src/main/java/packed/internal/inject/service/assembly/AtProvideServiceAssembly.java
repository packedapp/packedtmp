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
package packed.internal.inject.service.assembly;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.config.ConfigSiteInjectOperations;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.service.ServiceManager;
import packed.internal.inject.service.runtime.ConstantInjectorEntry;
import packed.internal.inject.service.runtime.PrototypeInjectorEntry;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.inject.sidecar.AtProvides;

/**
 *
 */
public class AtProvideServiceAssembly<T> extends ServiceAssembly<T> {

    private final Injectable injectable;

    private final int regionIndex;

    /**
     * Creates a new node from an instance.
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AtProvideServiceAssembly(ServiceManager im, ComponentNodeConfiguration compConf, AtProvides ap) {
        super(im, compConf.configSite().thenAnnotatedMember(ConfigSiteInjectOperations.INJECTOR_PROVIDE, ap.provides, ap.member), (Key) ap.key);
        this.injectable = new Injectable(this, compConf.source, ap);
        this.regionIndex = ap.isConstant ? compConf.region.reserve() : -1;
    }

    @Override
    public Injectable getInjectable() {
        return injectable;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceInstantiationContext context) {
        if (regionIndex == -1) {
            return new PrototypeInjectorEntry<>(this, context.region, dependencyAccessor());
        } else {
            return new ConstantInjectorEntry<>(this, context.region, regionIndex);
        }
    }

    @Override
    public int regionIndex() {
        return regionIndex;
    }

    @Override
    public MethodHandle dependencyAccessor() {
        return injectable.buildMethodHandle();
    }

    @Override
    public String toString() {
        return "@Provide " + injectable.directMethodHandle;
    }
}
