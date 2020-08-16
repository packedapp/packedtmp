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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerDescriptor;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.artifact.PackedAssemblyContext;
import packed.internal.component.BundleConfiguration;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedComponentDriver;

/** The default container context. */
public final class PackedContainerRole {

    private static final int LS_0_MAINL = 0;

    private static final int LS_1_LINKING = 1;

    private static final int LS_2_HOSTING = 2;

    public static final int LS_3_FINISHED = 3;

    /** Any extension that is active. */
    @Nullable
    public PackedExtensionConfiguration activeExtension;

    public ComponentNodeConfiguration component;

    int containerState;

    /** All used extensions, in order of registration. */
    public final LinkedHashMap<Class<? extends Extension>, PackedExtensionConfiguration> extensions = new LinkedHashMap<>();

    private TreeSet<PackedExtensionConfiguration> extensionsOrdered;

    public void advanceTo(int newState) {
        if (containerState == 0) {
            // We need to sort all extensions that are used. To make sure
            // they progress in their lifecycle in the right order.
            extensionsOrdered = new TreeSet<>(extensions.values());
            for (PackedExtensionConfiguration pec : extensionsOrdered) {
                activeExtension = pec;
                pec.onConfigured();
            }
            activeExtension = null;
            containerState = LS_1_LINKING;
        }

        if (containerState == LS_1_LINKING && newState > LS_1_LINKING) {
            for (ComponentNodeConfiguration cc = component.firstChild; cc != null; cc = cc.nextSibling) {
                if (cc.driver().isContainer()) {
                    cc.container.advanceTo(LS_3_FINISHED);
                }
            }
            for (PackedExtensionConfiguration pec : extensionsOrdered) {
                activeExtension = pec;
                pec.onChildrenConfigured();
            }
        }
    }

    public void buildDescriptor(ContainerDescriptor.Builder builder) {
        builder.setBundleDescription(component.getDescription());
        builder.setName(component.getName());
        for (PackedExtensionConfiguration e : extensions.values()) {
            e.buildDescriptor(builder);
        }
        builder.extensions.addAll(extensions.keySet());
    }

    public Set<Class<? extends Extension>> extensions() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    /**
     * Returns the context for the specified extension type. Or null if no extension of the specified type has already been
     * added.
     * 
     * @param extensionType
     *            the type of extension to return a context for
     * @return an extension's context, iff the specified extension type has already been added
     * @see #use(Class)
     * @see #useExtension(Class, PackedExtensionConfiguration)
     */
    @Nullable
    public PackedExtensionConfiguration getExtensionContext(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.get(extensionType);
    }

    // Previously this method returned the specified bundle. However, to encourage people to configure the bundle before
    // calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have void return
    // type. Maybe in the future LinkedBundle<- (LinkableContainerSource)
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");

        // Extract the driver from the bundle
        PackedComponentDriver<?> driver = BundleConfiguration.driverOf(bundle);

        // check if container

        // IDK do we want to progress to next stage just in case...
        if (containerState == LS_0_MAINL) {
            advanceTo(LS_1_LINKING);
        } else if (containerState == LS_2_HOSTING) {
            throw new IllegalStateException("Was hosting");
        } else if (containerState == LS_3_FINISHED) {
            throw new IllegalStateException("Was Assembled");
        }

        // Create the child node
        ComponentNodeConfiguration newNode = driver.newNodeConfiguration(component, bundle, wirelets);

        // Invoke Bundle::configure
        BundleConfiguration.configure(bundle, driver.forBundleConf(newNode));

        newNode.finalState = true;
    }

    @SuppressWarnings("unchecked")
    public <T extends Extension> T use(Class<T> extensionType) {
        return (T) useExtension(extensionType, null).instance();
    }

    /**
     * If an extension of the specified type has not already been installed, installs it. Returns the extension's context.
     * 
     * @param extensionType
     *            the type of extension
     * @param caller
     *            non-null if it is another extension that is requesting the extension
     * @return the extension's context
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     * @throws InternalExtensionException
     *             if the
     */
    PackedExtensionConfiguration useExtension(Class<? extends Extension> extensionType, @Nullable PackedExtensionConfiguration caller) {
        requireNonNull(extensionType, "extensionType is null");
        PackedExtensionConfiguration pec = extensions.get(extensionType);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (pec == null) {
            // Checks that we are still configurable
            if (caller == null) {
                if (containerState != 0) {
                    // Cannot perform this operation
                    throw new IllegalStateException("Cannot install new extensions at this point, extensionType = " + extensionType);
                }
                component.checkConfigurable();
            } else {
                caller.checkConfigurable();
            }
            extensions.put(extensionType, pec = PackedExtensionConfiguration.of(this, extensionType));

            // Add a component configuration node
            PackedComponentDriver<?> pcd = new PackedComponentDriver.ExtensionComponentDriver(ExtensionModel.of(extensionType));
            new ComponentNodeConfiguration(component, pcd, component.configSite(), pec.realm(), null, this);
        }
        return pec;
    }

    // From Driver,
    public static PackedContainerRole create(PackedComponentDriver<?> driver, ConfigSite cs, PackedRealm realm, ComponentNodeConfiguration parent,
            PackedAssemblyContext output, Wirelet... wirelets) {
        PackedContainerRole p1 = new PackedContainerRole();
        ComponentNodeConfiguration pccc = new ComponentNodeConfiguration(parent, driver, cs, realm, output, p1, wirelets);
        p1.component = pccc;
        return p1;
    }

    @Nullable
    public static PackedContainerRole findOrNull(ComponentNodeConfiguration cnc) {
        return cnc.containerOld;
    }
}
//
///**
//* Returns whether or not the specified extension type has been used.
//* 
//* @param extensionType
//*            the extension type to test.
//* @return whether or not the extension has been used
//*/
//boolean isExtensionUsed(Class<? extends Extension> extensionType) {
//  requireNonNull(extensionType, "extensionType is null");
//  return extensions.containsKey(extensionType);
//}
// Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
// has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
// loop situations, for example, if a bundle recursively links itself which fails by throwing
// java.lang.StackOverflowError instead of an infinite loop.
