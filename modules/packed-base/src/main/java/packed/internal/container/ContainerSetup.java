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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.container.sandbox.AssemblyBuildHook;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.InternalExtensionException;
import packed.internal.application.ApplicationSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.RealmSetup;
import packed.internal.inject.dependency.ContainerInjectorSetup;
import packed.internal.lifetime.LifetimeSetup;
import packed.internal.util.ClassUtil;

/** Build-time configuration of a container. */
public final class ContainerSetup extends ComponentSetup {

    public final AssemblyModel assemblyModel;

    /** Child containers, lazy initialized. */
    @Nullable
    public ArrayList<ContainerSetup> containerChildren;

    /** The depth of this container in relation to other containers. */
    public final int containerDepth;

    /** This container's parent (if non-root). */
    @Nullable
    public final ContainerSetup containerParent;

    /** All extensions in use, in no particular order. */
    final IdentityHashMap<Class<? extends Extension>, ExtensionSetup> extensions = new IdentityHashMap<>();

    private boolean hasRunPreContainerChildren;

    /** The injector of this container. */
    public final ContainerInjectorSetup injection = new ContainerInjectorSetup(this);

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    public boolean nameInitializedWithWirelet;

    private ArrayList<ExtensionSetup> tmpExtensions;

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
    public ContainerSetup(ApplicationSetup application, RealmSetup realm, LifetimeSetup lifetime, PackedContainerDriver<?> driver,
            @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(application, realm, lifetime, parent);

        this.assemblyModel = AssemblyModel.of(realm.realmType());

        // The rest of the constructor is just processing any wirelets that have been specified by
        // the user or extension when wiring the component. The wirelet's have not been null checked.
        // and may contained any number of CombinedWirelet instances.
        requireNonNull(wirelets, "wirelets is null");
        Wirelet prefix = null;
        if (application.container == null) {
            prefix = application.applicationDriver.wirelet;
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
        if (parent == null) {
            this.containerParent = null;
            this.containerDepth = 0;
        } else {
            this.containerParent = parent.container;
            this.containerDepth = containerParent.depth + 1;

            // Add this container to the children of the parent
            this.containerParent.runPredContainerChildren();
            ArrayList<ContainerSetup> c = containerParent.containerChildren;
            if (c == null) {
                c = containerParent.containerChildren = new ArrayList<>(5);
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

    public void applyAssemblyHook(AssemblyBuildHook hook) {
        // Puha, vi har jo ikke rigtig lyst til at dele en ContainerConfiguration
        // der lige pludselig kan have andre rettigheder.
        // Teoretisk attack mulighed, spawn en ny traad med configurationen.
        // Hvor vi haaber at ramme den lige praecis som vi har lyst til

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
        Collections.sort(extensionsOrdered, (c1, c2) -> -c1.model.compareTo(c2.model));

        // Close every extension
        for (ExtensionSetup extension : extensionsOrdered) {
            extension.onComplete();
        }

        injection.resolve();
    }

    /** {@return a unmodifiable view of all extension types that are in use.} */
    public Set<Class<? extends Extension>> extensionsTypes() {
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

    public void postBuild(ContainerConfiguration configuration) {
        assemblyModel.postBuild(configuration);
    }

    public void preBuild(ContainerConfiguration configuration) {
        assemblyModel.preBuild(configuration);
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
        // Det vi i virkeligheden har brug for er en cursor...
        // ind i extensions
        for (ExtensionSetup ea : extensions.values()) {
            ea.preContainerChildren();
        }

        // Den fungere ikke 100% den her loesning...
        // Hvir vi bruger nyte extensions... skal de jo helst koeres paa den rigtige plads...
        while (tmpExtensions != null) {
            ArrayList<ExtensionSetup> te = tmpExtensions;
            tmpExtensions = null;
            for (ExtensionSetup ea : te) {
                ea.preContainerChildren();
            }
        }
    }

    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
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
                realm.checkOpen();
            } else {
                requestedBy.checkIsPreCompletion();
            }

            // Create the new extension and adds into the map of extensions
            extension = ExtensionSetup.newExtension(this, extensionClass);

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
                    throw new InternalExtensionException(implementation +  " must be annotated with @ExtensionMember");
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
        public Set<Class<? extends Extension>> extensionsTypes() {
            return ContainerSetup.this.extensionsTypes();
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

        @Override
        public Optional<Class<? extends Extension>> managedByExtension() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ContainerMirror (" + path() + ")";
        }
    }
}
