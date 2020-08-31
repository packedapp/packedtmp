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
package packed.internal.lifecycle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;

import app.packed.component.Component;
import app.packed.guest.Guest;
import app.packed.lifecycleold.StopOption;
import app.packed.service.ServiceRegistry;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.util.LookupUtil;

/**
 * An instantiation context is created for every delimited tree hierachy.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */

/**
 * An artifact instantiation context is created every time an artifact is being instantiated.
 * <p>
 * Describes which phases it is available from
 * <p>
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> Hmmm what about if use them at startup???
 */
public final class PackedInitializationContext {

    public static final MethodHandle MH_COMPONENT = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "component", Component.class);

    public static final MethodHandle MH_GUEST = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "guest", Guest.class);

    public static final MethodHandle MH_SERVICES = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "services", ServiceRegistry.class);

    private ComponentNode node;

    private final WireletPack wirelets;

    private PackedInitializationContext(WireletPack wirelets) {
        this.wirelets = wirelets;
    }

    /**
     * Returns a list of wirelets that used to instantiate. This may include wirelets that are not present at build time if
     * using an image.
     * 
     * @return a list of wirelets that used to instantiate
     */
    public WireletPack wirelets() {
        return wirelets;
    }

    public Component component() {
        return node;
    }

    public Guest guest() {
        return new DummyGuest();
    }

    public ServiceRegistry services() {
        return (ServiceRegistry) node.data[0];
    }

    public static PackedInitializationContext initialize(ComponentNodeConfiguration root, WireletPack wp) {
        PackedInitializationContext ic = new PackedInitializationContext(wp);
        ic.node = root.instantiateTree(ic);
        return ic;
    }

    static class DummyGuest implements Guest {

        /** {@inheritDoc} */
        @Override
        public Guest start() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public <T> CompletableFuture<T> startAsync(T result) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Guest stop(StopOption... options) {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public <T> CompletableFuture<T> stopAsync(T result, StopOption... options) {
            return null;
        }

    }
}
