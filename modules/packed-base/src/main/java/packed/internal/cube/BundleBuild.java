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
package packed.internal.cube;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.TreeSet;

import app.packed.base.Nullable;
import app.packed.bundle.Extension;
import app.packed.bundle.InternalExtensionException;
import app.packed.inject.ServiceExtension;
import packed.internal.component.ComponentBuild;
import packed.internal.component.BuildtimeRegion;
import packed.internal.inject.Dependant;
import packed.internal.inject.service.ServiceBuildManager;
import packed.internal.inject.service.ServiceSubSystem;

/** Contains data and logic relevant for containers. */
public final class BundleBuild {

    /** Child containers, lazy initialized */
    @Nullable
    public ArrayList<BundleBuild> children;

    /** The component this container is a part of. */
    public final ComponentBuild compConf;

    /** All dependants that needs to be resolved. */
    public final ArrayList<Dependant> dependants = new ArrayList<>();

    /** All extensions that have been used, in any order. */
    private final IdentityHashMap<Class<? extends Extension>, ExtensionBuild> extensions = new IdentityHashMap<>();

    boolean hasRunPreContainerChildren;

    @Nullable
    private Boolean isImage;

    /** Any parent container this container might have. */
    @Nullable
    public final BundleBuild parent;

    /** A service manager that handles everything to do with services, is lazily initialized. */
    @Nullable
    private ServiceBuildManager sbm;

    private ArrayList<ExtensionBuild> tmpExtensions;

    /**
     * Creates a new container
     * 
     * @param compConf
     *            the configuration of the component the container is a part of
     */
    public BundleBuild(ComponentBuild compConf) {
        this.compConf = requireNonNull(compConf);

        this.parent = compConf.getParent() == null ? null : compConf.getParent().getMemberOfContainer();
        if (parent != null) {
            parent.runPredContainerChildren();
            ArrayList<BundleBuild> c = parent.children;
            if (c == null) {
                c = parent.children = new ArrayList<>();
            }
            c.add(this);
        }
    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param dependant
     *            the injectable to add
     */
    public void addDependant(Dependant dependant) {
        dependants.add(requireNonNull(dependant));

        // Bliver noedt til at lave noget sidecar preresolve her.
        // I virkeligheden vil vi bare gerne checke at om man
        // har ting der ikke kan resolves via contexts
        if (sbm == null && !dependant.dependencies.isEmpty()) {
            useExtension(ServiceExtension.class);
        }
    }

    public void close(BuildtimeRegion region) {
        if (!hasRunPreContainerChildren) {
            runPredContainerChildren();
        }
        // Complete all extensions in order
        // Vil faktisk mene det skal vaere den modsatte order...
        // Tror vi skal have vendt comparatoren
        TreeSet<ExtensionBuild> extensionsOrdered = new TreeSet<>(extensions.values());
        for (ExtensionBuild pec : extensionsOrdered) {
            pec.complete();
        }

        // Resolve local services
        if (sbm != null) {
            sbm.prepareDependants();
        }

        for (Dependant i : dependants) {
            i.resolve(sbm);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

        if (sbm != null) {
            sbm.dependencies().checkForMissingDependencies(this);
        }

        // TODO Check any contracts we might as well catch it early

        // If we form for a service island and is root of the island
        // Do checks here
        boolean isIslandChild = sbm != null && parent != null && parent.sbm != null;
        if (!isIslandChild) {
            ServiceSubSystem.finish(region, this);
        }
    }

    /**
     * Returns an unmodifiable view of the extension registered with this container.
     * 
     * @return a unmodifiable view of the extension registered with this container
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
     * @see #useExtension(Class, ExtensionBuild)
     */
    @Nullable
    public ExtensionBuild getExtensionContext(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.get(extensionType);
    }

    @Nullable
    public ServiceBuildManager getServiceManager() {
        return sbm;
    }

    public ServiceBuildManager getServiceManagerOrCreate() {
        ServiceBuildManager s = sbm;
        if (s == null) {
            useExtension(ServiceExtension.class);
            s = sbm;
        }
        return s;
    }

    public boolean isPartOfImage() {
        Boolean b = isImage;
        if (b != null) {
            return b;
        }
        ComponentBuild cc = compConf.getParent();
        while (cc != null) {
            if (cc.modifiers().isImage()) {
                return isImage = Boolean.TRUE;
            }
            cc = cc.getParent();
        }
        return isImage = Boolean.FALSE;
    }

    public ServiceBuildManager newServiceManagerFromServiceExtension() {
        return sbm = new ServiceBuildManager(this);
    }

    private void runPredContainerChildren() {
        if (hasRunPreContainerChildren) {
            return;
        }
        hasRunPreContainerChildren = true;
        if (extensions.isEmpty()) {
            return;
        }
        // We have a problem here... We need to
        // keep track of extensions that are added in this step..
        // And run ea.preContainerChildren on them...
        // And then repeat until some list/set has not been touched...
        for (ExtensionBuild ea : extensions.values()) {
            ea.preContainerChildren();
        }

        while (tmpExtensions != null) {
            ArrayList<ExtensionBuild> te = tmpExtensions;
            tmpExtensions = null;
            for (ExtensionBuild ea : te) {
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
    ExtensionBuild useExtension(Class<? extends Extension> extensionType, @Nullable ExtensionBuild caller) {
        requireNonNull(extensionType, "extensionType is null");
        ExtensionBuild extension = extensions.get(extensionType);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (extension == null) {
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
            // Create the new extension
            extension = ExtensionBuild.of(this, extensionType);

            // Add the extension to the extension map
            extensions.put(extensionType, extension);

            if (hasRunPreContainerChildren) {
                ArrayList<ExtensionBuild> l = tmpExtensions;
                if (l == null) {
                    l = tmpExtensions = new ArrayList<>();
                }
                l.add(extension);
            }
        }
        return extension;
    }

    @SuppressWarnings("unchecked")
    public <T extends Extension> T useExtension(Class<T> extensionType) {
        return (T) useExtension(extensionType, null).instance();
    }
}
