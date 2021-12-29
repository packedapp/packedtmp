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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.ComponentMirror;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;
import packed.internal.application.ApplicationSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.inject.dependency.ContainerInjectorSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.ClassUtil;
import packed.internal.util.CollectionUtil;

/** Build-time configuration of a container. */
public final class ContainerSetup extends ComponentSetup {

    /** Children of this node (lazily initialized) in insertion order. */
    public final LinkedHashMap<String, ComponentSetup> children = new LinkedHashMap<>();

    /** Child containers, lazy initialized. */
    /// Hmm just iterate through all?? except for benchmarks it shouldnt really be a problem
    @Nullable
    public ArrayList<ContainerSetup> containerChildren;

    /** All extensions in use in this container. */
    public final LinkedHashMap<Class<? extends Extension>, ExtensionSetup> extensions = new LinkedHashMap<>();

    /** The injector of this container. */
    // I think move this to BeanSetup
    public final ContainerInjectorSetup injection = new ContainerInjectorSetup(this);

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    public boolean nameInitializedWithWirelet;

    /** Wirelets that was specified when creating the component. */
    // Alternativ er den ikke final.. men bliver nullable ud eftersom der ikke er flere wirelets
    @Nullable
    public final WireletWrapper wirelets;

    /**
     * Create a new container (component) setup.
     * 
     * @param application
     *            the application this container is a part of
     * @param realm
     *            the realm this container is a part of
     * @param driver
     *            the driver that is used to create this container
     * @param parent
     *            any parent component
     * @param wirelets
     *            optional wirelets specified when creating or wiring the container
     */
    public ContainerSetup(ApplicationSetup application, RealmSetup realm, LifetimeSetup lifetime, PackedContainerDriver driver, @Nullable ContainerSetup parent,
            Wirelet[] wirelets) {
        super(application, realm, lifetime, parent);

        // The rest of the constructor is just processing any wirelets that have been specified by
        // the user or extension when wiring the component. The wirelet's have not been null checked.
        // and may contained any number of CombinedWirelet instances.
        requireNonNull(wirelets, "wirelets is null");
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

            if (nameInitializedWithWirelet && parent != null) {
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

    /** {@return a unmodifiable view of all extension types that are in use.} */
    public Set<Class<? extends Extension>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    /**
     * Returns whether or not the specified extension type is used.
     * 
     * @param extensionType
     *            the extension to test
     * @return true if the specified extension type is used, otherwise false
     * @see ContainerConfiguration#isExtensionUsed(Class)
     * @see ExtensionConfiguration#isExtensionUsed(Class)
     * @see ContainerMirror#isExtensionUsed(Class)
     */
    public boolean isExtensionUsed(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.containsKey(extensionType);
    }

    /** {@return a container mirror.} */
    @Override
    public ContainerMirror mirror() {
        return new BuildTimeContainerMirror();
    }

    public void onRealmClose() {
        // We recursively close all children in the same realm first
        // We do not close individual components
        if (containerChildren != null) {
            for (ContainerSetup c : containerChildren) {
                if (c.realm == realm) {
                    c.onRealmClose();
                }
            }
        }
        // Complete all extensions in order
        // Vil faktisk mene det skal vaere den modsatte order...
        // Tror vi skal have vendt comparatoren

        // Close every extension
//        for (ExtensionSetup extension : extensionsOrdered) {
//     //       extension.onComplete();
//        }

        injection.resolve();
    }

    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        throw new UnsupportedOperationException();
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
    ExtensionSetup useExtension(Class<? extends Extension> extensionClass, @Nullable ExtensionSetup requestedByExtension) {
        requireNonNull(extensionClass, "extensionClass is null");
        ExtensionSetup extension = extensions.get(extensionClass);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onNew.
        // Which would then fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (extension == null) {

            // Checks that container is still configurable
            if (requestedByExtension == null) {
                // A user has made a request, that requires an extension to be installed.
                // Check that the realm is still open

                // TODO check that the extensionClass is not banned for users

                realm.checkOpen();
            } else {
                // An extension has made a request, that requires an extension to be installed.

                // TODO check that the extensionClass is not banned for users

                // TODO Check that the extension user model has not been closed
                requestedByExtension.checkUserConfigurable();
            }

            // make sure it is recursively installed into the root container
            ExtensionSetup extensionParent = parent == null ? null : parent.useExtension(extensionClass, requestedByExtension);

            // Create a extension and initialize it.
            extension = new ExtensionSetup(extensionParent, this, extensionClass);
            extension.initialize();
        }
        return extension;
    }

    @SuppressWarnings("unchecked")
    public <E extends Extension> E useExtension(Class<E> extensionClass) {
        realm.newOperation();
        ExtensionSetup extension = useExtension(extensionClass, /* requested by the user, not another extension */ null);
        return (E) extension.instance(); // extract the extension instance
    }

    /** A build-time container mirror. */
    private final class BuildTimeContainerMirror extends ComponentSetup.AbstractBuildTimeComponentMirror implements ContainerMirror {

        /** Extracts the extension that */
        private static final ClassValue<Class<? extends Extension>> MIRROR_TO_EXTENSION_EXTRACTOR = new ClassValue<>() {

            /** {@inheritDoc} */
            protected Class<? extends Extension> computeValue(Class<?> implementation) {
                ClassUtil.checkProperSubclass(ExtensionMirror.class, implementation);

                ExtensionMember em = implementation.getAnnotation(ExtensionMember.class);
                if (em == null) {
                    throw new InternalExtensionException(implementation + " must be annotated with @ExtensionMember");
                }
                Class<? extends Extension> extensionType = em.value();
                ClassUtil.checkProperSubclass(Extension.class, extensionType); // move into type extractor?

                // Den
                ClassUtil.checkProperSubclass(Extension.class, extensionType, InternalExtensionException::new); // move into type extractor?

                // Ved ikke om den her er noedvendig??? Vi checker jo om den type extensionen
                // returnere matcher
                if (extensionType.getModule() != implementation.getModule()) {
                    throw new InternalExtensionException("The extension mirror " + implementation + " must be a part of the same module ("
                            + extensionType.getModule() + ") as " + extensionType + ", but was part of '" + implementation.getModule() + "'");
                }
                return extensionType;
            }
        };

        /** {@inheritDoc} */
        public final Collection<ComponentMirror> children() {
            return CollectionUtil.unmodifiableView(children.values(), c -> c.mirror());
        }

        @Override
        public Optional<Class<? extends Extension>> declaredByExtension() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Set<ExtensionMirror> extensions() {
            HashSet<ExtensionMirror> result = new HashSet<>();
            for (ExtensionSetup extension : extensions.values()) {
                result.add(extension.mirror());
            }
            return Set.copyOf(result);
        }

        /** {@inheritDoc} */
        @Override
        public Set<Class<? extends Extension>> extensionTypes() {
            return ContainerSetup.this.extensionTypes();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public <T extends ExtensionMirror> Optional<T> findExtension(Class<T> mirrorType) {
            requireNonNull(mirrorType, "mirrorType is null");

            // First find what extension the mirror belongs to by extracting <E> from ExtensionMirror<E extends Extension>
            Class<? extends Extension> cl = MIRROR_TO_EXTENSION_EXTRACTOR.get(mirrorType);

            // See if the container uses the extension.
            ExtensionSetup extension = extensions.get(cl);
            if (extension == null) {
                return Optional.empty();
            } else {
                // Call the extension.mirror to create a new mirror, this method is most likely overridden
                ExtensionMirror mirror = extension.mirror();
                // Fail if the type of mirror returned by the extension does not match the specified mirror type
                if (!mirrorType.isInstance(mirror)) {
                    // Kan maaske smide en specific fejlmeddelse hvis man ikke har overskrevet metoden
                    // if isMethodOvreriden()
                    /// throw new (.mirror must be overridden and return an instance of Fooo
                    throw new InternalExtensionException(cl.getSimpleName() + ".mirror() was expected to return an instance of " + mirrorType
                            + ", but returned an instance of " + mirror.getClass());
                }
                return (Optional<T>) Optional.of(mirror);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isExtensionUsed(Class<? extends Extension> extensionType) {
            return ContainerSetup.this.isExtensionUsed(extensionType);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ContainerMirror (" + path() + ")";
        }
    }
}

