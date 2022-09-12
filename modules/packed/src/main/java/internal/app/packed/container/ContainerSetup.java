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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.inject.service.ContainerInjectionManager;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of a container. */
public final class ContainerSetup extends ComponentSetup {

    /** A MethodHandle for invoking {@link ContainerMirror#initialize(ContainerSetup)}. */
    private static final MethodHandle MH_CONTAINER_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ContainerMirror.class,
            "initialize", void.class, ContainerSetup.class);

    /** Supplies a mirror for the container. */
    public final Supplier<? extends ContainerMirror> mirrorSupplier = ContainerMirror::new;

    /** Children of this node in insertion order. */
    // Maybe have an extra List just with beans? IDK
    public final LinkedHashMap<String, ComponentSetup> children = new LinkedHashMap<>();

    /** Children that are containers (subset of ContainerSetup.children), lazy initialized. */
    @Nullable
    public ArrayList<ContainerSetup> containerChildren;

    /**
     * All extensions used by this container. We keep them in a LinkedHashMap so that {@link #extensionTypes()} returns a
     * deterministic view.
     */
    public final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

    /** The container's injection manager. */
    public final ContainerInjectionManager injectionManager;

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    public boolean isNameInitializedFromWirelet;

    /** Wirelets that was specified when creating the component. */
    // As an alternative non-final, and then nulled out whenever the last wirelet is consumed
    @Nullable
    public final WireletWrapper wirelets;

    /**
     * Create a new container setup.
     * 
     * @param application
     *            the application this container is a part of
     * @param realm
     *            the realm this container is a part of
     * @param handle
     *            the driver that is used to create this container
     * @param parent
     *            any parent container
     * @param wirelets
     *            optional wirelets specified when creating or wiring the container
     */
    public ContainerSetup(ApplicationSetup application, UserRealmSetup realm, PackedContainerDriver handle, @Nullable ContainerSetup parent,
            Wirelet[] wirelets) {
        super(application, realm, parent);
        requireNonNull(wirelets, "wirelets is null");

        this.injectionManager = new ContainerInjectionManager(this);

        // The rest of the constructor is just processing any wirelets that have been specified by
        // the user or extension when wiring the component. The wirelet's have not been null checked.
        // and may contained any number of CombinedWirelet instances.
        Wirelet prefix = null;
        if (application.container == null) {
            prefix = application.driver.wirelet;
        }

        if (wirelets.length == 0 && prefix == null) {
            this.wirelets = null;
        } else {
            // If it is the root
            Wirelet[] ws;
            if (prefix == null) {
                ws = CompositeWirelet.flattenAll(wirelets);
            } else {
                ws = CompositeWirelet.flatten2(prefix, Wirelet.combine(wirelets));
            }

            this.wirelets = new WireletWrapper(ws);

            // May initialize the component's name, onWire, ect
            // Do we need to consume internal wirelets???
            // Maybe that is what they are...
            int unconsumed = 0;
            for (Wirelet w : ws) {
                if (w instanceof InternalWirelet bw) {
                    // Maaske er alle internal wirelets first passe
                    bw.onBuild(this);
                } else {
                    unconsumed++;
                }
            }
            if (unconsumed > 0) {
                this.wirelets.unconsumed = unconsumed;
            }

            if (isNameInitializedFromWirelet && parent != null) {
                initializeNameWithPrefix(name);
                // addChild(child, name);
            }
        }

        // Various container tree-node management
        if (parent != null) {
            // Add this container to the children of the parent
            ArrayList<ContainerSetup> c = parent.containerChildren;
            if (c == null) {
                c = parent.containerChildren = new ArrayList<>(5);
            }
            c.add(this);
        }

        // Set the name of the container if it was not set by a wirelet
        if (name == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = null;

            // TODO Should only be used on the root container in the assembly
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

    /** {@return a unmodifiable view of all extension types that are in use in no particular order.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    /**
     * Returns whether or not the specified extension type is used.
     * 
     * @param extensionType
     *            the extension to test
     * @return true if the specified extension type is used, otherwise false
     * @see ContainerConfiguration#isExtensionUsed(Class)
     * @see ContainerMirror#isExtensionUsed(Class)
     */
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.containsKey(extensionType);
    }

    /** {@return a new mirror.} */
    @Override
    public ContainerMirror mirror() {
        // Create a new ContainerMirror
        ContainerMirror mirror = mirrorSupplier.get();
        if (mirror == null) {
            throw new NullPointerException(mirrorSupplier + " returned a null instead of an " + ContainerMirror.class.getSimpleName() + " instance");
        }

        // Initialize ContainerMirror by calling ContainerMirror#initialize(ContainerSetup)
        try {
            MH_CONTAINER_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ComponentSetup> stream() {
        return Stream.concat(Stream.of(this), children.values().stream().flatMap(c -> c.stream()));
    }

    /**
     * Returns an extension of the specified type.
     * <p>
     * If this is the first time an extension of the specified type has been requested. This method will create a new
     * instance of the extension. This instance will then be returned for all subsequent requests for the same extension
     * type.
     * 
     * @param <E>
     *            the type of extension to return
     * @param extensionClass
     *            the Class object corresponding to the extension type
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if this container is no longer configurable and the specified type of extension has not been used
     *             previously
     */
    @SuppressWarnings("unchecked")
    public <E extends Extension<?>> E useExtension(Class<E> extensionClass) {
        ExtensionSetup extension = useExtensionSetup(extensionClass, /* requested by the user, not another extension */ null);
        return (E) extension.instance();
    }

    /**
     * If an extension of the specified type has not already been installed, installs it. Returns the extension's context.
     * 
     * @param extensionClass
     *            the type of extension
     * @param requestedByExtension
     *            non-null if it is another extension that is requesting the extension
     * @return the extension's context
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     * @throws InternalExtensionException
     *             if the
     */
    // Any dependencies needed have been checked
    public ExtensionSetup useExtensionSetup(Class<? extends Extension<?>> extensionClass, @Nullable ExtensionSetup requestedByExtension) {
        requireNonNull(extensionClass, "extensionClass is null");
        ExtensionSetup extension = extensions.get(extensionClass);

        // We do not use #computeIfAbsent, because extensions might install other extensions when initializing.
        // Which would then fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (extension == null) {
            // Ny extensions skal installeres indefor Assembly::build

            if (realm.isClosed()) {
                throw new IllegalStateException("Cannot install new extensions as the container is no longer configurable");
            }
            // Checks that container is still configurable
            if (requestedByExtension == null) {
                // A user has made a request, that requires an extension to be installed.
                // Check that the realm is still open

                // TODO check that the extensionClass is not banned for users

            } else {
                // An extension has made a request, that requires an extension to be installed.

                // TODO check that the extensionClass is not banned for users

                // TODO Check that the extension user model has not been closed
                if (requestedByExtension.extensionRealm.isClosed()) {
                    throw new IllegalStateException();
                }
            }

            // The extension must be recursively installed into the root container if not already installed in parent
            ExtensionSetup extensionParent = parent == null ? null : parent.useExtensionSetup(extensionClass, requestedByExtension);

            // Create the extension. (This will also add an entry to #extensions)
            extension = ExtensionSetup.newExtension(extensionParent, this, extensionClass);
        }
        return extension;
    }
}
