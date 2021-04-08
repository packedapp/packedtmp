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
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedComponentModifierSet;
import packed.internal.component.RealmSetup;
import packed.internal.component.WireableComponentDriver.ContainerComponentDriver;
import packed.internal.component.WireableComponentSetup;
import packed.internal.inject.dependency.ContainerInjectorSetup;

/** Build-time configuration of a container. */
public final class ContainerSetup extends WireableComponentSetup {

    /** Child containers, lazy initialized. */
    @Nullable
    public ArrayList<ContainerSetup> containerChildren;

    /** This container's parent (if non-root). */
    @Nullable
    public final ContainerSetup containerParent;

    /** All extensions in use, in no particular order. */
    final IdentityHashMap<Class<? extends Extension>, ExtensionSetup> extensions = new IdentityHashMap<>();

    private boolean hasRunPreContainerChildren;

    /** The injector of this container. */
    public final ContainerInjectorSetup injection = new ContainerInjectorSetup(this);

    private ArrayList<ExtensionSetup> tmpExtensions;

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
     *            the driver that was used to create the container
     * @param parent
     *            any parent component
     * @param wirelets
     *            optional wirelets specified when creating or wiring the container
     */
    public ContainerSetup(ApplicationSetup application, RealmSetup realm, ContainerComponentDriver driver, @Nullable ComponentSetup parent,
            Wirelet[] wirelets) {
        super(application, realm, driver, parent, wirelets);

        // Various container tree management
        this.containerParent = parent == null ? null : parent.container;
        if (containerParent != null) {
            containerParent.runPredContainerChildren();
            ArrayList<ContainerSetup> c = containerParent.containerChildren;
            if (c == null) {
                c = containerParent.containerChildren = new ArrayList<>(5);
            }
            c.add(this);
        }

        // Set the name of the component if it was not set by a wirelet
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
            } else {
                n = "Unknown";
            }
            initializeNameWithPrefix(n);
        }
        assert name != null;
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

        injection.resolve();
    }

    /** {@return a container adaptor that can be exposed to end-users} */
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
     * Returns whether or not the specified extension is in use.
     * 
     * @param extensionClass
     *            the extension to test
     * @return true if the specified extension is in use, otherwise false
     */
    public boolean isUsed(Class<? extends Extension> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        return extensions.containsKey(extensionClass);
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
        throw new UnsupportedOperationException("This operation is not supported for a container");
    }

    @Override
    public void sourceProvide() {
        throw new UnsupportedOperationException("This operation is not supported for a container");
    }

    @Override
    public void sourceProvideAs(Key<?> key) {
        throw new UnsupportedOperationException("This operation is not supported for a container");
    }

    @Override
    public Optional<Key<?>> sourceProvideAsKey() {
        throw new UnsupportedOperationException("This operation is not supported for a container");
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
                realm.checkOpen();
            } else {
                requestedBy.checkOpen();
            }

            // Create the new extension and adds into the map of extensions
            extension = ExtensionSetup.newInstance(this, extensionClass);

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
        realm.newOperation();
        return (T) useExtension(extensionClass, null).extensionInstance();
    }

    /** A Container adaptor. */
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
