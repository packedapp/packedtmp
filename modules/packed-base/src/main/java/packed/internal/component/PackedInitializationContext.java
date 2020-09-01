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
package packed.internal.component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.component.Component;
import app.packed.guest.Guest;
import app.packed.service.ServiceRegistry;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.util.LookupUtil;

/**
 * An instantiation context is created every time an artifact is being instantiated.
 * <p>
 * Describes which phases it is available from
 * <p>
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */
public final class PackedInitializationContext {

    /** A MethodHandle that can invoke {@link #component()}. */
    public static final MethodHandle MH_COMPONENT = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "component", Component.class);

    /** A MethodHandle that can invoke {@link #guest()}. */
    public static final MethodHandle MH_GUEST = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "guest", Guest.class);

    /** A MethodHandle that can invoke {@link #services()}. */
    public static final MethodHandle MH_SERVICES = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "services", ServiceRegistry.class);

    /** The component node we are building. */
    private ComponentNode node;

    private final WireletPack wirelets;

    private PackedInitializationContext(WireletPack wirelets) {
        this.wirelets = wirelets;
    }

    public Component component() {
        return node;
    }

    public Guest guest() {
        return node.store.getGuest(node);
    }

    // Initialize name, we don't want to override this in Configuration context. We don't want the conf to change if
    // image...
    // Check for any runtime wirelets that have been specified.
    // This is probably not the right way to do it. Especially with hosts.. Fix it when we get to hosts...
    // Maybe this can be written in PodInstantiationContext
    String rootName(ComponentNodeConfiguration configuration) {
        String n = configuration.name;
        String ol = wirelets() == null ? null : wirelets().nameWirelet();
        if (ol != null) {
            n = ol;
            if (n.endsWith("?")) {
                n = n.substring(0, n.length() - 1);
            }
        }
        return n;
    }

    public ServiceRegistry services() {
        // if !container return empty registry...
        return node.store.getServiceRegistry(node);
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

    public static PackedInitializationContext initialize(ComponentNodeConfiguration root) {
        PackedInitializationContext ic = new PackedInitializationContext(root.wirelets);
        ic.node = new ComponentNode(null, root, ic);
        return ic;
    }

    public static PackedInitializationContext initializeImage(ComponentNodeConfiguration root, WireletPack wirelets) {
        PackedInitializationContext ic = new PackedInitializationContext(wirelets);
        ic.node = new ComponentNode(null, root, ic);
        return ic;
    }
}