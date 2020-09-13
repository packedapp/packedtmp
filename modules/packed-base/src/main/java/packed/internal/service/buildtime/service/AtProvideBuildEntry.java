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
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.SourceAssembly;
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

    final SourceAssembly source;

    /**
     * Creates a new node from an instance.
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AtProvideBuildEntry(ConfigSite configSite, ComponentNodeConfiguration component, AtProvides ap) {
        super(component.container.im, configSite);
        this.source = component.source;
        this.injectable = new Injectable(this, source, ap);
        this.key = (Key) ap.key;
        if (ap.isConstant) {
            this.regionIndex = component.region.reserve();
        } else {
            this.regionIndex = -1;
        }
        component.injectionManager().provider().buildEntries.add(this);
    }

    @Override
    @Nullable
    public Injectable injectable() {
        return injectable;
    }

    @Override
    public int regionIndex() {
        return regionIndex;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        if (regionIndex > -1) {
            return new ConstantInjectorEntry<>(this, context.region, regionIndex);
        } else {
            return new PrototypeInjectorEntry<>(this, context.region, toMethodHandle());
        }
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
