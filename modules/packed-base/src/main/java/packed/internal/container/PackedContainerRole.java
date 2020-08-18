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
import app.packed.container.ContainerDescriptor;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.component.ComponentNodeConfiguration;

/** The default container context. */
public final class PackedContainerRole {

    public static final int LS_0_MAINL = 0;

    public static final int LS_1_LINKING = 1;

    public static final int LS_2_HOSTING = 2;

    public static final int LS_3_FINISHED = 3;

    public int containerState;

    /** All used extensions, in order of registration. */
    public final LinkedHashMap<Class<? extends Extension>, PackedExtensionConfiguration> extensions = new LinkedHashMap<>();

    private TreeSet<PackedExtensionConfiguration> extensionsOrdered;

    /** The component node this container belongs to. */
    final ComponentNodeConfiguration node;

    @Nullable
    public final PackedContainerRole parent;

    public PackedContainerRole(ComponentNodeConfiguration node) {
        this.node = requireNonNull(node);
        this.parent = node.parentOrNull() == null ? null : node.parentOrNull().container();
    }

    public void advanceTo(int newState) {
        if (containerState == 0) {
            // We need to sort all extensions that are used. To make sure
            // they progress in their lifecycle in the right order.
            extensionsOrdered = new TreeSet<>(extensions.values());
            for (PackedExtensionConfiguration pec : extensionsOrdered) {
                pec.onConfigured();
            }
            containerState = LS_1_LINKING;
        }

        if (containerState == LS_1_LINKING && newState > LS_1_LINKING) {
            for (ComponentNodeConfiguration cc = node.firstChild; cc != null; cc = cc.nextSibling) {
                if (cc.driver().isContainer()) {
                    cc.container().advanceTo(LS_3_FINISHED);
                }
            }
            for (PackedExtensionConfiguration pec : extensionsOrdered) {
                pec.onChildrenConfigured();
            }
        }
    }

    public void buildDescriptor(ContainerDescriptor.Builder builder) {
        builder.setName(node.getName());
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
    public PackedExtensionConfiguration getContext(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.get(extensionType);
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
                node.checkConfigurable();
            } else {
                caller.checkConfigurable();
            }

            // Tror lige vi skal have gennemtaenkt den lifecycle...
            // Taenker om vi
            extensions.put(extensionType, pec = PackedExtensionConfiguration.of(this, extensionType));
        }
        return pec;
    }
}
