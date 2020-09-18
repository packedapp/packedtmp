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
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.inject.InjectionManager;

/** Contains data and logic relevant for containers. */
public final class ContainerAssembly {

    /** Child containers, lazy initialized */
    @Nullable
    private ArrayList<ContainerAssembly> children;

    /** The component this container is a part of. */
    public final ComponentNodeConfiguration compConf;

    /** All used extensions, in order of registration. */
    public final LinkedHashMap<Class<? extends Extension>, ExtensionAssembly> extensions = new LinkedHashMap<>();

    boolean hasRunPreContainerChildren;

    public final InjectionManager im;

    /** Any parent container this container might have */
    @Nullable
    public final ContainerAssembly parent;

    ArrayList<ExtensionAssembly> tmpExtension;

    /**
     * Creates a new container
     * 
     * @param compConf
     *            the configuration of the component the container is a part of
     */
    public ContainerAssembly(ComponentNodeConfiguration compConf) {
        this.compConf = requireNonNull(compConf);
        this.parent = compConf.getParent() == null ? null : compConf.getParent().memberOfContainer();
        if (parent != null) {
            parent.runPredContainerChildren();

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

    /**
     * Returns a set view of the extension registered with this container.
     * 
     * @return a set view of the extension registered with this container
     */
    public Set<Class<? extends Extension>> extensionView() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    public void finish() {
        if (!hasRunPreContainerChildren) {
            runPredContainerChildren();
        }
        TreeSet<ExtensionAssembly> extensionsOrdered = new TreeSet<>(extensions.values());
        for (ExtensionAssembly pec : extensionsOrdered) {
            pec.completed();
        }
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

    private void runPredContainerChildren() {
        if (hasRunPreContainerChildren) {
            return;
        }
        hasRunPreContainerChildren = true;
        // We have a problem here... We need to
        // keep track of extensions that are added in this step..
        // And run ea.preContainerChildren on them...
        // And then repeat until some list/set has not been touched...
        if (!extensions.isEmpty()) {
            for (ExtensionAssembly ea : extensions.values()) {
                ea.preContainerChildren();
            }
        }
        while (tmpExtension != null) {
            ArrayList<ExtensionAssembly> te = tmpExtension;
            tmpExtension = null;
            for (ExtensionAssembly ea : te) {
                ea.preContainerChildren();
            }
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
            if (children != null) {
                throw new IllegalStateException(
                        "Cannot install new extensions after child containers have been added to this container, extensionType = " + extensionType);
            }

            // Checks that we are still configurable
            if (caller == null) {
                compConf.checkConfigurable();
            } else {
                caller.checkConfigurable();
            }
            // Tror lige vi skal have gennemtaenkt den lifecycle...
            // Taenker om vi
            extensions.put(extensionType, pec = ExtensionAssembly.of(this, extensionType));

            if (hasRunPreContainerChildren) {
                ArrayList<ExtensionAssembly> l = tmpExtension;
                if (l == null) {
                    l = tmpExtension = new ArrayList<>();
                }
                l.add(pec);
            }
        }
        return pec;
    }

    @SuppressWarnings("unchecked")
    public <T extends Extension> T useExtension(Class<T> extensionType) {
        return (T) useExtension(extensionType, null).instance();
    }
}
