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
package packed.internal.service.buildtime;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.inject.Injectable;
import packed.internal.inject.sidecar.AtProvides;
import packed.internal.inject.various.ConfigSiteInjectOperations;
import packed.internal.service.InjectionManager;
import packed.internal.service.runtime.ConstantInjectorEntry;
import packed.internal.service.runtime.PrototypeInjectorEntry;
import packed.internal.service.runtime.RuntimeService;
import packed.internal.service.runtime.ServiceInstantiationContext;

/**
 *
 */
public class AtProvideBuildEntry<T> extends BuildtimeService<T> {

    private final Injectable injectable;

    private final int regionIndex;

    /**
     * Creates a new node from an instance.
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AtProvideBuildEntry(InjectionManager im, ComponentNodeConfiguration compConf, AtProvides ap) {
        super(im, compConf.configSite().thenAnnotatedMember(ConfigSiteInjectOperations.INJECTOR_PROVIDE, ap.provides, ap.member), (Key) ap.key);
        this.injectable = new Injectable(this, compConf.source, ap);
        this.regionIndex = ap.isConstant ? compConf.region.reserve() : -1;
    }

    @Override
    public Injectable injectable() {
        return injectable;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceInstantiationContext context) {
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
