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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;

import app.packed.container.Assembly;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerInstaller;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.lifetime.ContainerLifetimeTemplate;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;

/** Implementation of {@link ContainerInstaller}. */
public final class PackedContainerInstaller implements ContainerInstaller {

    /** The application we are installing the container into. */
    // I think once we get app-on-app this is Nullable
    final ApplicationSetup application;

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    /** The lifetime of the container being installed. */
    final PackedContainerLifetimeTemplate lifetime;

    String nameFromWirelet;

    /** The parent of container being installed. Or <code>null</code> if a root container. */
    @Nullable
    final ContainerSetup parent;

    public PackedContainerInstaller(ContainerLifetimeTemplate lifetime, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        this.lifetime = (PackedContainerLifetimeTemplate) requireNonNull(lifetime, "lifetime is null");
        this.application = requireNonNull(application);
        this.parent = parent;
        this.installedBy = requireNonNull(installedBy);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerHandle install(Assembly assembly, Wirelet... wirelets) {
        parent.assembly.checkIsConfigurable();

        // Create a new assembly, which call into #containerInstall
        AssemblySetup as = new AssemblySetup(null, null, this, assembly, wirelets);

        // Build the assembly
        as.build();

        return new PackedContainerHandle(as.container);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerHandle install(Wirelet... wirelets) {
        parent.assembly.checkIsConfigurable();
        ContainerSetup container = newContainer(parent.assembly, wirelets);
        return new PackedContainerHandle(container);
    }

    public ContainerSetup newContainer(AssemblySetup assembly, Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");

        // Most of this method is just processing wirelets
        Wirelet prefix = application.driver.wirelet();

        // We do not current set Container.WW
        WireletWrapper ww = null;

        String name = null;

        if (wirelets.length > 0 || prefix != null) {
            // If it is the root
            Wirelet[] ws;
            if (prefix == null) {
                ws = CompositeWirelet.flattenAll(wirelets);
            } else {
                ws = CompositeWirelet.flatten2(prefix, Wirelet.combine(wirelets));
            }

            ww = new WireletWrapper(ws);

            // May initialize the component's name, onWire, ect
            // Do we need to consume internal wirelets???
            // Maybe that is what they are...
            int unconsumed = 0;
            for (Wirelet w : ws) {
                if (w instanceof InternalWirelet bw) {
                    // Maaske er alle internal wirelets first passe
                    bw.onInstall(this);
                } else {
                    unconsumed++;
                }
            }
            if (unconsumed > 0) {
                ww.unconsumed = unconsumed;
            }
        }

        // Set the name of the container if it was not set by a wirelet
        if (nameFromWirelet == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = null;

            // TODO Should only be used on the root container in the assembly
            Class<? extends Assembly> source = assembly.assembly.getClass();
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
            name = n;
        } else {
            name = nameFromWirelet;
        }

        String n = name;
        if (parent != null) {
            HashMap<String, Object> c = parent.children;
            if (c.size() == 0) {
                c.put(n, this);
            } else {
                int counter = 1;
                while (c.putIfAbsent(n, this) != null) {
                    n = n + counter++; // maybe store some kind of map<ComponentSetup, LongCounter> in BuildSetup.. for those that want to test
                                       // adding 1
                    // million of the same component type
                }
            }
        }

        // Create the new extension
        ContainerSetup container = new ContainerSetup(this, assembly, n);

        // BaseExtension is automatically used by every container
        ExtensionSetup.install(BaseExtension.class, container, null);

        return container;
    }

    /** A wirelet that will set the name of the component. Used by {@link Wirelet#named(String)}. */
    public static final class OverrideNameWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final String name;

        /**
         * Creates a new name wirelet
         *
         * @param name
         *            the name to override any existing container name with
         */
        public OverrideNameWirelet(String name) {
            this.name = NameCheck.checkComponentName(name); // throws IAE
        }

        /** {@inheritDoc} */
        @Override
        public void onImageInstantiation(ContainerSetup c, ApplicationInitializationContext ic) {
            ic.name = name;
        }

        /** {@inheritDoc} */
        @Override
        public void onInstall(PackedContainerInstaller installer) {
            installer.nameFromWirelet = name;// has already been validated
        }
    }
}
