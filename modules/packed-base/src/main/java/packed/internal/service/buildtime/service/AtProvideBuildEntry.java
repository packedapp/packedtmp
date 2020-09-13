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

import app.packed.base.Key;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.inject.Injectable;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.runtime.ConstantInjectorEntry;
import packed.internal.service.runtime.PrototypeInjectorEntry;
import packed.internal.service.runtime.RuntimeService;

/**
 *
 */
public class AtProvideBuildEntry<T> extends BuildtimeService<T> {

    public final Injectable injectable;

    public final int regionIndex;

    /**
     * Creates a new node from an instance.
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AtProvideBuildEntry(ComponentNodeConfiguration compConf, AtProvides ap) {
        super(compConf.container.im, compConf.configSite().thenAnnotatedMember(ConfigSiteInjectOperations.INJECTOR_PROVIDE, ap.provides, ap.member),
                (Key) ap.key);
        this.injectable = new Injectable(this, compConf.source, ap);
        if (ap.isConstant) {
            this.regionIndex = compConf.region.reserve();
        } else {
            this.regionIndex = -1;
        }
        compConf.injectionManager().buildEntries.add(this);
        compConf.region.allInjectables.add(injectable);
    }

    @Override
    public Injectable injectable() {
        return injectable;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        if (regionIndex == -1) {
            return new PrototypeInjectorEntry<>(this, context.region, toMethodHandle());
        } else {
            return new ConstantInjectorEntry<>(this, context.region, regionIndex);
        }
    }

    @Override
    public int regionIndex() {
        return regionIndex;
    }

    @Override
    public MethodHandle toMethodHandle() {
        return injectable.buildMethodHandle();
    }

    @Override
    public String toString() {
        return "@Provide " + injectable.directMethodHandle;
    }
}
