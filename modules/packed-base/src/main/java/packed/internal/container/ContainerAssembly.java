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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.service.ServiceExtension;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.service.InjectionManager;

/** The default container context. */
public final class ContainerAssembly {

    public static final int LS_0_MAINL = 0;

    public static final int LS_1_LINKING = 1;

    public static final int LS_2_HOSTING = 2;

    public static final int LS_3_FINISHED = 3;

    /** The component this container is a part of. */
    public final ComponentNodeConfiguration compConf;

    public int containerState;

    /** All used extensions, in order of registration. */
    public final LinkedHashMap<Class<? extends Extension>, ExtensionAssembly> extensions = new LinkedHashMap<>();

    private TreeSet<ExtensionAssembly> extensionsOrdered;

    public final InjectionManager im;

    /** Any parent container this container might have */
    @Nullable
    public final ContainerAssembly parent;

    @Nullable
    private ArrayList<ContainerAssembly> children;

    /**
     * Creates a new container
     * 
     * @param compConf
     *            the configuration of the component the container is a part of
     */
    public ContainerAssembly(ComponentNodeConfiguration compConf) {
        this.compConf = requireNonNull(compConf);
        this.parent = compConf.getParent() == null ? null : compConf.getParent().container();
        if (parent != null) {

            ArrayList<ContainerAssembly> c = parent.children;
            if (c == null) {
                c = parent.children = new ArrayList<>();
            }
            c.add(this);
        }
        this.im = new InjectionManager(this);
    }

    public void checkNoChildContainers() {
        if (children != null) {
            throw new IllegalStateException();
        }
    }

    public void advanceTo(int newState) {
        if (containerState == 0) {
            // We need to sort all extensions that are used. To make sure
            // they progress in their lifecycle in the right order.
            extensionsOrdered = new TreeSet<>(extensions.values());
            for (ExtensionAssembly pec : extensionsOrdered) {
                pec.onConfigured();
            }
            containerState = LS_1_LINKING;
        }

        if (containerState == LS_1_LINKING && newState > LS_1_LINKING) {
            for (ComponentNodeConfiguration cc = compConf.treeFirstChild; cc != null; cc = cc.treeNextSibling) {
                if (cc.container != null) {
                    cc.container.advanceTo(LS_3_FINISHED);
                }
            }
            for (ExtensionAssembly pec : extensionsOrdered) {
                pec.onChildrenConfigured();
            }
        }
    }

    /**
     * Returns a set view of the extension registered with this container.
     * 
     * @return a set view of the extension registered with this container
     */
    public Set<Class<? extends Extension>> extensionView() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    /**
     * Returns the context for the specified extension type. Or null if no extension of the specified type has already been
     * added.
     * 
     * @param extensionType
     *            the type of extension to return a context for
     * @return an extension's context, iff the specified extension type has already been added
     * @see #useExtension(Class)
     * @see #useExtension(Class, ExtensionAssembly)
     */
    @Nullable
    public ExtensionAssembly getExtensionContext(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.get(extensionType);
    }

    public void hackServiceExtension() {
        // A hack to allow ServiceContract to ServiceExtension
        // As it is not installed just for missing requiremeents
        // see ServiceContractTCKTest
        if (!extensions.containsKey(ServiceExtension.class)) {
            extensions.put(ServiceExtension.class, ExtensionAssembly.of(this, ServiceExtension.class));
        }
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
    ExtensionAssembly useExtension(Class<? extends Extension> extensionType, @Nullable ExtensionAssembly caller) {
        requireNonNull(extensionType, "extensionType is null");
        ExtensionAssembly pec = extensions.get(extensionType);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (pec == null) {
            // Checks that we are still configurable
            if (caller == null) {
                if (children != null) {
                    throw new IllegalStateException(
                            "Cannot install new extensions after child containers have been added to this container, extensionType = " + extensionType);
                }
                if (containerState != 0) {
                    // Cannot perform this operation
                    throw new IllegalStateException("Cannot install new extensions at this point, extensionType = " + extensionType);
                }
                compConf.checkConfigurable();
            } else {
                caller.checkConfigurable();
            }

            // Tror lige vi skal have gennemtaenkt den lifecycle...
            // Taenker om vi
            extensions.put(extensionType, pec = ExtensionAssembly.of(this, extensionType));
        }
        return pec;
    }

    @SuppressWarnings("unchecked")
    public <T extends Extension> T useExtension(Class<T> extensionType) {
        return (T) useExtension(extensionType, null).instance();
    }
}
