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
import java.util.IdentityHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.container.Assembly;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.ContainerLocal;
import app.packed.extension.Extension;
import app.packed.extension.container.ContainerBuilder;
import app.packed.extension.container.ContainerTemplate;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.lifetime.PackedContainerLifetimeChannel;
import internal.app.packed.lifetime.runtime.ApplicationInitializationContext;

/** Implementation of {@link ContainerBuilder}. */
public final class PackedContainerBuilder extends AbstractContainerBuilder implements ContainerBuilder {

    /** The application we are installing the container into. */
    // I think once we get app-on-app this is Nullable
    final ApplicationSetup application;

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    /** Locals that the container is initialized with. */
    final IdentityHashMap<PackedContainerLocal<?>, Object> locals = new IdentityHashMap<>();

    String name;

    String nameFromWirelet;

    boolean newApplication;

    /** The parent of container being installed. Or <code>null</code> if a root container. */
    @Nullable
    final ContainerSetup parent;

    /** The template for the new container. */
    final PackedContainerTemplate template;

    boolean isUsed;

    // Cannot take ExtensionSetup, as BaseExtension is not instantiated for a root container
    private PackedContainerBuilder(ContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        this.template = (PackedContainerTemplate) requireNonNull(template, "template is null");
        this.application = requireNonNull(application);
        this.parent = parent;
        this.installedBy = requireNonNull(installedBy);
    }

    private void checkNotUsed() {

    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle build(Assembly assembly, Wirelet... wirelets) {
        parent.assembly.checkIsConfigurable();
        checkNotUsed();

        // Create a new assembly, which call into #containerInstall
        AssemblySetup as = new AssemblySetup(null, application.goal, this, assembly, wirelets);

        // Build the assembly
        as.build();

        return new PackedContainerHandle(as.container);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle build(Wirelet... wirelets) {
        checkNotUsed();

        parent.assembly.checkIsConfigurable();
        ContainerSetup container = newContainer(parent.assembly, wirelets);
        return new PackedContainerHandle(container);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle buildAndUseThisExtension(Wirelet... wirelets) {
        checkNotUsed();

        parent.assembly.checkIsConfigurable();
        ContainerSetup container = newContainer(parent.assembly, wirelets);
        container.useExtension(installedBy, null);
        return new PackedContainerHandle(container);
    }

    @SuppressWarnings("unchecked")
    public <T> ContainerBuilder consumeLocal(ContainerLocal<T> local, Consumer<T> action) {
        PackedContainerLocal<?> pcl = (PackedContainerLocal<?>) local;
        action.accept((T) pcl.get(this));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerBuilder named(String name) {
        this.name = name;
        return this;
    }

    public ContainerSetup newContainer(AssemblySetup assembly, Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");

        // Most of this method is just processing wirelets
        Wirelet prefix = application.driver.wirelet();

        // We do not current set Container.WW
        WireletWrapper ww = null;

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

        String nn = null;

        // Set the name of the container if it was not set by a wirelet
        if (nameFromWirelet == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = name;

            if (n == null) {
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
            }
            nn = n;
        } else {
            nn = nameFromWirelet;
        }

        String n = nn;
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

    /** {@inheritDoc} */
    @Override
    public <T> ContainerBuilder setLocal(ContainerLocal<T> local, T value) {
        locals.put((PackedContainerLocal<?>) local, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void specializeMirror(Supplier<? extends ContainerMirror> supplier) {
        throw new UnsupportedOperationException();
    }

    public static PackedContainerBuilder of(ContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        PackedContainerBuilder pcb = new PackedContainerBuilder(template, installedBy, application, parent);
        for (PackedContainerLifetimeChannel b : application.driver.channels()) {
            b.use(pcb);
        }
        return pcb;
    }

    public static final class NewApplicationWirelet extends InternalWirelet {

        /** {@inheritDoc} */
        @Override
        public void onInstall(PackedContainerBuilder installer) {
            if (installer.parent == null) {
                throw new Error("This wirelet cannot be used when creating a new application");
            }
            installer.newApplication = true;
        }

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
        public void onInstall(PackedContainerBuilder installer) {
            installer.nameFromWirelet = name;// has already been validated
        }
    }
}
