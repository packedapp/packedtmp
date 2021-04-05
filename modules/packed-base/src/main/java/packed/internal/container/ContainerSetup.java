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
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

import app.packed.application.Application;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Assembly;
import app.packed.component.ComponentAttributes;
import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;
import app.packed.container.Container;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.inject.ServiceExtension;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.BuildSetup;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireableComponentDriver;
import packed.internal.component.WireableComponentSetup;
import packed.internal.inject.dependency.InjectionNode;
import packed.internal.inject.service.ServiceManagerSetup;

/** Build-time configuration of a container. */
public final class ContainerSetup extends WireableComponentSetup {

    /** Child containers, lazy initialized (we rely on this in ExtensionSetup) */
    @Nullable
    public ArrayList<ContainerSetup> containerChildren;

    /** This container's parent (if non-root). */
    @Nullable
    public final ContainerSetup containerParent;

    /** All extensions in use, in no particular order. */
    final IdentityHashMap<Class<? extends Extension>, ExtensionSetup> extensions = new IdentityHashMap<>();

    boolean hasRunPreContainerChildren;

    /** A service manager that handles everything to do with services, is lazily initialized. */
    @Nullable
    private ServiceManagerSetup sm;

    private ArrayList<ExtensionSetup> tmpExtensions;

    public final ContainerInjectorSetup cis = new ContainerInjectorSetup();

    /**
     * Create a new container (component).
     * 
     * @param build
     *            the build this container is a part of
     * @param application
     *            the application this container is a part of
     * @param realm
     *            the realm this container is a part of
     * @param driver
     *            the driver that was used to create the container, is always
     *            {@link WireableComponentDriver.ContainerComponentDriver}
     * @param parent
     *            any parent component
     * @param wirelets
     *            optional wirelets specified when creating or wiring the container
     */
    public ContainerSetup(BuildSetup build, ApplicationSetup application, RealmSetup realm, WireableComponentDriver<?> driver, @Nullable ComponentSetup parent,
            Wirelet[] wirelets) {
        super(build, application, realm, driver, parent, wirelets);

        this.containerParent = parent == null ? null : parent.container;
        if (containerParent != null) {
            containerParent.runPredContainerChildren();
            ArrayList<ContainerSetup> c = containerParent.containerChildren;
            if (c == null) {
                c = containerParent.containerChildren = new ArrayList<>(5);
            }
            c.add(this);
        }

        // Set the name of the component if it have not already been set using a wirelet
        if (name == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = null;
            Class<?> source = realm.realmType();
            if (Assembly.class.isAssignableFrom(source)) {
                String nnn = source.getSimpleName();
                if (nnn.length() > 8 && nnn.endsWith("Assembly")) {
                    nnn = nnn.substring(0, nnn.length() - 8);
                }
                if (nnn.length() > 0) {
                    // checkName, if not just App
                    // TODO need prefix
                    n = nnn;
                }
                if (nnn.length() == 0) {
                    n = "Assembly";
                }
            }
            initializeNameWithPrefix(n);
        }
    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param dependant
     *            the injectable to add
     */
    public void addNode(InjectionNode dependant) {
        cis.nodes.add(requireNonNull(dependant));

        // Bliver noedt til at lave noget sidecar preresolve her.
        // I virkeligheden vil vi bare gerne checke at om man
        // har ting der ikke kan resolves via contexts
        if (sm == null && !dependant.dependencies.isEmpty()) {
            useExtension(ServiceExtension.class);
        }
    }

    @Override
    protected void attributesAdd(DefaultAttributeMap dam) {
        // kan ogsaa test om container.application = application.container?
        if (PackedComponentModifierSet.isSet(modifiers, ComponentModifier.APPLICATION)) {
            PackedApplicationDriver<?> pac = application.driver;
            dam.addValue(ComponentAttributes.APPLICATION_CLASS, pac.artifactRawType());
        }

    }

    public void closeRealm() {
        // We recursively close all children in the same realm first
        // We do not close individual components
        if (containerChildren != null) {
            for (ContainerSetup c : containerChildren) {
                if (c.realm == realm) {
                    c.closeRealm();
                }
            }
        }

        if (!hasRunPreContainerChildren) {
            runPredContainerChildren();
        }
        // Complete all extensions in order
        // Vil faktisk mene det skal vaere den modsatte order...
        // Tror vi skal have vendt comparatoren
        ArrayList<ExtensionSetup> extensionsOrdered = new ArrayList<>(extensions.values());
        Collections.sort(extensionsOrdered, (c1, c2) -> -c1.model().compareTo(c2.model()));

        // Close every extension
        for (ExtensionSetup extension : extensionsOrdered) {
            extension.onComplete();
        }

        // Resolve local services
        if (sm != null) {
            sm.prepareDependants(this);
        }

        for (InjectionNode i : cis.nodes) {
            i.resolve(sm);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

        if (sm != null) {
            sm.dependencies().checkForMissingDependencies(this);
            sm.close(this, pool);
        }
        // TODO Check any contracts we might as well catch it early
    }

    public Container containerAdaptor() {
        return new ContainerAdaptor(this);
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
     * @param extensionClass
     *            the type of extension to return a context for
     * @return an extension's context, iff the specified extension type has already been added
     * @see #useExtension(Class)
     */
    @Nullable
    public ExtensionSetup getExtensionContext(Class<? extends Extension> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        return extensions.get(extensionClass);
    }

    @Nullable
    public ServiceManagerSetup getServiceManager() {
        return sm;
    }

    public ServiceManagerSetup getServiceManagerOrCreate() {
        ServiceManagerSetup s = sm;
        if (s == null) {
            useExtension(ServiceExtension.class);
            s = sm;
        }
        return s;
    }

    /**
     * Returns whether or not the specified extension is in use.
     * 
     * @param extensionClass
     *            the extension to test
     * @return true if the specified extension is in use, otherwise false
     */
    public boolean isInUse(Class<? extends Extension> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        return extensions.containsKey(extensionClass);
    }

    /**
     * This method is invoked from the constructor of a {@link ServiceExtension} to create a new
     * {@link ServiceManagerSetup}.
     * 
     * @return the new service manager
     */
    public ServiceManagerSetup newServiceManagerFromServiceExtension() {
        return sm = new ServiceManagerSetup(sm);
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
        for (ExtensionSetup ea : extensions.values()) {
            ea.preContainerChildren();
        }

        while (tmpExtensions != null) {
            ArrayList<ExtensionSetup> te = tmpExtensions;
            tmpExtensions = null;
            for (ExtensionSetup ea : te) {
                ea.preContainerChildren();
            }
        }
    }

    @Override
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sourceProvide() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sourceProvideAs(Key<?> key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Key<?>> sourceProvideAsKey() {
        throw new UnsupportedOperationException();
    }

    /**
     * If an extension of the specified type has not already been installed, installs it. Returns the extension's context.
     * 
     * @param extensionClass
     *            the type of extension
     * @param requestedBy
     *            non-null if it is another extension that is requesting the extension
     * @return the extension's context
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     * @throws InternalExtensionException
     *             if the
     */
    // Any dependencies needed have been checked
    ExtensionSetup useExtension(Class<? extends Extension> extensionClass, @Nullable ExtensionSetup requestedBy) {
        requireNonNull(extensionClass, "extensionClass is null");
        ExtensionSetup extension = extensions.get(extensionClass);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onNew.
        // Which would then fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (extension == null) {
            if (containerChildren != null) {
                throw new IllegalStateException(
                        "Cannot install new extensions after child containers have been added to this container, extensionClass = " + extensionClass);
            }

            // Checks that container is still configurable
            if (requestedBy == null) {
                checkOpen();
            } else {
                requestedBy.checkOpen();
            }

            // Create the new extension and adds into the map of extensions
            extension = ExtensionSetup.createNew(this, extensionClass);

            if (hasRunPreContainerChildren) {
                ArrayList<ExtensionSetup> l = tmpExtensions;
                if (l == null) {
                    l = tmpExtensions = new ArrayList<>();
                }
                l.add(extension);
            }
        }
        return extension;
    }

    @SuppressWarnings("unchecked")
    public <T extends Extension> T useExtension(Class<T> extensionClass) {
        realm.wireLatest();
        return (T) useExtension(extensionClass, null).extensionInstance();
    }

    /** An adaptor of {@code ContainerSetup->Container} */
    record ContainerAdaptor(ContainerSetup container) implements Container {

        /** {@inheritDoc} */
        @Override
        public Application application() {
            return container.application.adaptor();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return container.getName();
        }
    }
}
