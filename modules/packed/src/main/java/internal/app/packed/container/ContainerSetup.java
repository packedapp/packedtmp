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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.ComponentMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.bean.BeanMirror;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.inject.service.ContainerInjectionManager;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.StreamUtil;

/** The internal configuration of a container. */
public final class ContainerSetup extends ComponentSetup {

    /** Children of this node in insertion order. */
    // Maybe have an extra List just with beans? IDK
    public final LinkedHashMap<String, ComponentSetup> children = new LinkedHashMap<>();

    /** Children that are containers (subset of ContainerSetup.children), lazy initialized. */
    @Nullable
    public ArrayList<ContainerSetup> containerChildren;

    /**
     * All extensions used by this container. We keep them in a LinkedHashMap so that {@link #extensionTypes()} returns
     * a deterministic view.
     */
    final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

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
    public ContainerSetup(ApplicationSetup application, UserRealmSetup realm, PackedContainerDriver handle, @Nullable ContainerSetup parent, Wirelet[] wirelets) {
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

    /** {@return a container mirror.} */
    @Override
    public BuildTimeContainerMirror mirror() {
        return new BuildTimeContainerMirror(this);
    }

    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ComponentSetup> stream() {
        return Stream.concat(Stream.of(this), children.values().stream().flatMap(c -> c.stream()));
    }

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

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onNew.
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

            // make sure it is recursively installed into the root container
            ExtensionSetup extensionParent = parent == null ? null : parent.useExtensionSetup(extensionClass, requestedByExtension);

            // Create a extension and initialize it.
            extension = new ExtensionSetup(extensionParent, this, extensionClass);

            extension.initialize();
        }
        return extension;
    }

    /**
     * A build-time container mirror.
     * 
     * @implNote previous versions had a common super class shared between BeanSetup and ContainerSetup. However, code is
     *           much cleaner without it. So please don't reintroduce it.
     * 
     */
    public record BuildTimeContainerMirror(ContainerSetup container) implements ContainerMirror {

        /** {@inheritDoc} */
        public Collection<ContainerMirror> children() {
            return CollectionUtil.unmodifiableView(container.containerChildren, c -> c.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public Set<ExtensionMirror<?>> extensions() {
            HashSet<ExtensionMirror<?>> result = new HashSet<>();
            for (ExtensionSetup extension : container.extensions.values()) {
                result.add(ExtensionMirrorHelper.newMirrorOfUnknownType(extension));
            }
            return Set.copyOf(result);
        }

        /** {@inheritDoc} */
        @Override
        public Set<Class<? extends Extension<?>>> extensionTypes() {
            return container.extensionTypes();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public <T extends ExtensionMirror<?>> Optional<T> findExtension(Class<T> mirrorType) {
            ClassUtil.checkProperSubclass(ExtensionMirror.class, mirrorType, "mirrorType");
            return (Optional<T>) Optional.ofNullable(ExtensionMirrorHelper.newMirrorOrNull(container, mirrorType));
        }

        /** {@inheritDoc} */
        @Override
        public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
            return container.isExtensionUsed(extensionType);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ContainerMirror (" + path() + ")";
        }

        /** {@inheritDoc} */
        @Override
        public Collection<BeanMirror> beans() {
            // return CollectionUtil.unmodifiableView(children.values(), c -> c.mirror());
            throw new UnsupportedOperationException();
            // we need a filter on the view...
            // size, isEmpty, is going to get a bit slower.
        }

        /** {@inheritDoc} */
        @Override
        public Stream<OperationMirror> operations() {
            return StreamUtil.filterAssignable(BeanSetup.class, container.children.values().stream()).flatMap(b -> b.mirror().operations());
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror application() {
            return container.application.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public AssemblyMirror assembly() {
            return container.userRealm.mirror();
        }

        /** {@inheritDoc} */
        public final Stream<ComponentMirror> stream() {
            return container.stream().map(c -> c.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            return container.depth;
        }

        /** {@inheritDoc} */
        @Override
        public LifetimeMirror lifetime() {
            return container.lifetime.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public String name() {
            return container.name;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<ContainerMirror> parent() {
            ContainerSetup p = container.parent;
            return p == null ? Optional.empty() : Optional.of(p.mirror());
        }

        /** {@inheritDoc} */
        @Override
        public NamespacePath path() {
            return container.path();
        }
    }
}
