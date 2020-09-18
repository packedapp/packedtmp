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
import app.packed.component.ComponentModifier;
import app.packed.guest.Guest;
import app.packed.service.ServiceRegistry;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.inject.InjectionManager;
import packed.internal.util.LookupUtil;

/**
 * An instantiation context is created every time an artifact is being instantiated.
 * <p>
 * Describes which phases it is available from
 * <p>
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */
// Ideen er vi skal bruge den til at registrere fejl...

// MethodHandle stableAccess(Object[] array) <-- returns 
public final class PackedInitializationContext {

    /** A MethodHandle for invoking {@link #component()}. */
    public static final MethodHandle MH_COMPONENT = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "component", Component.class);

    /** A MethodHandle for invoking {@link #guest()}. */
    public static final MethodHandle MH_GUEST = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "guest", Guest.class);

    /** A MethodHandle for invoking {@link #services()}. */
    public static final MethodHandle MH_SERVICES = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "services", ServiceRegistry.class);

    /** The component node we are building. */
    ComponentNode component;

    private final WireletPack wirelets;
    final ComponentNodeConfiguration root;

    private PackedInitializationContext(ComponentNodeConfiguration root, WireletPack wirelets) {
        this.root = root;
        this.wirelets = wirelets;
    }

    /**
     * Returns the top component.
     * 
     * @return the top component
     */
    public Component component() {
        return component;
    }

    public Guest guest() {
        if (component.hasModifier(ComponentModifier.GUEST)) {
            return component.region.guest();
        }
        throw new UnsupportedOperationException("This component does not have a guest");
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
        InjectionManager im = root.injectionManager();
        return im.newServiceRegistry(component, component.region, wirelets);
//                
//        /// Create a service registry...
//        // We do not stored this in the region but outside...
//        ContainerAssembly container = compConf.memberOfContainer;
//        int registryIndex = root.modifiers().isGuest() ? 1 : 0;
//        InjectionManager node = container.im;
//        // Move this to lazy create via PIC
//        // And no need to store this is the region
//        if (node != null) {
//            region.store(registryIndex, ));
//        } else {
//            region.store(registryIndex, ServiceRegistry.empty());
//        }
//
//        

        // TODO fix ServiceRegistry saa det er visibility fra Bundle/lookup klasse.
        // Som afgoer om en service er med...
        // Naahh er det ikke bare alt der bliver explicit exporteret????

        // TODO lav det lazy, hvis det bliver efterspurgt...
        // Ingen grund til det er i en regions node...
        // if !container return empty registry...
        // return component.region.serviceRegistry(component);
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
        PackedInitializationContext pic = new PackedInitializationContext(root, root.wirelets);
        // Hmmm Packed Guest bliver jo lavet der...
        // Maaske laver vi en PackedGuest og smider i PIC. som man saa kan steale...
        if (root.modifiers().isGuest()) {
            PackedGuest.initializeAndStart(root, pic);
        } else {
            new ComponentNode(null, root, pic);
        }
        return pic;
    }

    public static PackedInitializationContext initializeFromImage(ComponentNodeConfiguration root, WireletPack wirelets) {
        PackedInitializationContext pic = new PackedInitializationContext(root, wirelets);
        if (root.modifiers().isGuest()) {
            PackedGuest.initializeAndStart(root, pic);
        } else {
            new ComponentNode(null, root, pic);
        }
        return pic;
    }
}
