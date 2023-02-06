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

import app.packed.container.Assembly;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerInstaller;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import app.packed.lifetime.ContainerLifetimeTemplate;
import internal.app.packed.application.ApplicationSetup;

/** Implementation of {@link ContainerInstaller}. */
public final class PackedContainerInstaller implements ContainerInstaller {

    final ApplicationSetup application;

    /** The lifetime of the container being installed. */
    public final PackedContainerLifetimeTemplate lifetime;

    /** The parent of container being installed. Or <code>null</code> if a root container. */
    @Nullable
    final ContainerSetup parent;

    final Class<? extends Extension<?>> installedBy;

    public PackedContainerInstaller(ContainerLifetimeTemplate lifetime, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        this.lifetime = (PackedContainerLifetimeTemplate) requireNonNull(lifetime);
        this.application = requireNonNull(application);
        this.parent = parent;
        this.installedBy = requireNonNull(installedBy);
    }

    public PackedContainerInstaller(ContainerLifetimeTemplate lifetime, Class<? extends Extension<?>> installedBy, ContainerSetup parent) {
        this(lifetime, installedBy, parent.application, parent);
    }

    private void cleanup(ContainerSetup container, Wirelet[] wirelets) {
        // Install BaseExtension which is automatically used by every container
        ExtensionSetup.install(BaseExtension.class, container, null);

        // The rest of this method is just processing wirelets that have been specified by
        // the user or extension when wiring the component. The wirelets have not been null checked.
        // and may contain any number of CombinedWirelet instances.
        Wirelet prefix = null;
        if (application.container == null) {
            prefix = application.driver.wirelet();
        }

        if (wirelets.length == 0 && prefix == null) {
            container.wirelets = null;
        } else {
            // If it is the root
            Wirelet[] ws;
            if (prefix == null) {
                ws = CompositeWirelet.flattenAll(wirelets);
            } else {
                ws = CompositeWirelet.flatten2(prefix, Wirelet.combine(wirelets));
            }

            container.wirelets = new WireletWrapper(ws);

            // May initialize the component's name, onWire, ect
            // Do we need to consume internal wirelets???
            // Maybe that is what they are...
            int unconsumed = 0;
            for (Wirelet w : ws) {
                if (w instanceof InternalWirelet bw) {
                    // Maaske er alle internal wirelets first passe
                    bw.onBuild(container);
                } else {
                    unconsumed++;
                }
            }
            if (unconsumed > 0) {
                container.wirelets.unconsumed = unconsumed;
            }

            if (container.isNameInitializedFromWirelet && container.treeParent != null) {
                container.initializeNameWithPrefix(container.name);
                // addChild(child, name);
            }
        }

        // Set the name of the container if it was not set by a wirelet
        if (container.name == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = null;

            // TODO Should only be used on the root container in the assembly
            Class<? extends Assembly> source = container.assembly.assembly.getClass();
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
            container.initializeNameWithPrefix(n);
        }
        assert container.name != null;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerHandle install(Assembly assembly, Wirelet... wirelets) {
        parent.assembly.checkIsConfigurable();

        // Create a new assembly
        AssemblySetup as = new AssemblySetup(null, null, this, assembly, wirelets);

        // Build the assembly
        as.build();

        return new PackedContainerHandle(as.container);
    }

    public ContainerSetup install(AssemblySetup assembly, Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        ContainerSetup cs = new ContainerSetup(this, assembly);
        cleanup(cs, wirelets);
        return cs;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerHandle install(Wirelet... wirelets) {
        return new PackedContainerHandle(install(parent.assembly, wirelets));
    }
}
