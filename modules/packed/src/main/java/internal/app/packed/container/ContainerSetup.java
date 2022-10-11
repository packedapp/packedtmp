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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.operation.newInject.ServiceManager;
import internal.app.packed.service.InternalServiceExtension;
import internal.app.packed.util.InsertionOrderedTree;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of a container. */
public final class ContainerSetup extends InsertionOrderedTree<ContainerSetup> implements BeanOrContainerSetup {

    /** A MethodHandle for invoking {@link ContainerMirror#initialize(ContainerSetup)}. */
    private static final MethodHandle MH_CONTAINER_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ContainerMirror.class,
            "initialize", void.class, ContainerSetup.class);

    /** The application this component is a part of. */
    public final ApplicationSetup application;

    /** The assembly from where the component is being installed. */
    public final AssemblySetup assembly;

    public final Map<Class<?>, Object> beanClassMap = new HashMap<>(); // Must have unique beans unless multi

    @Nullable
    public BeanSetup beanFirst;

    @Nullable
    public BeanSetup beanLast;

    /** Maintains unique names for beans and child containers. */
    public final HashMap<String, Object> children = new HashMap<>();

    /** The depth of the component in the application tree. */
    public final int depth; // maintain in InsertionTree?

    /**
     * All extensions used by this container. We keep them in a LinkedHashMap so that {@link #extensionTypes()} returns a
     * deterministic view.
     */
    // Or maybe extension types is always sorted??
    public final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

    /** The container's injection manager. */
    public final InternalServiceExtension injectionManager;

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    public boolean isNameInitializedFromWirelet;

    /** The lifetime the component is a part of. */
    public final ContainerLifetimeSetup lifetime;

    /** Supplies a mirror for the container. */
    public final Supplier<? extends ContainerMirror> mirrorSupplier = ContainerMirror::new;

    /** The name of this component. */
    @Nullable
    private String name;

    /** The realm used to install this component. */
    public final RealmSetup realm;

    public final ServiceManager sm;

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
    public ContainerSetup(ApplicationSetup application, AssemblySetup realm, PackedContainerHandle handle, @Nullable ContainerSetup parent,
            Wirelet[] wirelets) {
        super(parent);
        this.realm = requireNonNull(realm);
        realm.wireNew(this);
        this.application = requireNonNull(application);
        this.assembly = realm;
        if (parent == null) {
            this.sm = new ServiceManager(null);
            this.depth = 0;
            this.lifetime = new ContainerLifetimeSetup((ContainerSetup) this, null);
        } else {
            this.sm = new ServiceManager(parent.sm);
            this.depth = parent.depth + 1;
            this.lifetime = parent.lifetime;
        }

        requireNonNull(wirelets, "wirelets is null");

        this.injectionManager = new InternalServiceExtension(this);

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

        // Set the name of the container if it was not set by a wirelet
        if (name == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = null;

            // TODO Should only be used on the root container in the assembly
            Class<? extends Assembly> source = realm.assembly.getClass();
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

    public int beanCount() {
        int count = 0;
        for (var b = beanFirst; b != null; b = b.nextBean) {
            count += 1;
        }
        return count;
    }

    /** {@return a unmodifiable view of all extension types that are in use in no particular order.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    public String getName() {
        return name;
    }

    public void initBeanName(BeanSetup bean, String name) {
        int size = children.size();
        if (size == 0) {
            children.put(name, this);
        } else {
            String n = name;

            while (children.putIfAbsent(n, this) != null) {
                n = name + size++; // maybe store some kind of map<ComponentSetup, LongCounter> in BuildSetup.. for those that want to test adding 1
                                   // million of the same component type
            }
        }
    }

    protected final void initializeNameWithPrefix(String name) {
        String n = name;
        if (treeParent != null) {
            HashMap<String, Object> c = treeParent.children;
            if (c.size() == 0) {
                c.put(name, this);
            } else {
                int counter = 1;
                while (c.putIfAbsent(n, this) != null) {
                    n = name + counter++; // maybe store some kind of map<ComponentSetup, LongCounter> in BuildSetup.. for those that want to test adding 1
                                          // million of the same component type
                }
            }
        }
        this.name = n;
    }

    public void checkIsCurrent() {
        if (!isCurrent()) {
            String errorMsg;
            // if (realm.container == this) {
            errorMsg = "This operation must be called as the first thing in Assembly#build()";
            // } else {
            // errorMsg = "This operation must be called immediately after the component has been wired";
            // }
            // is it just named(), in that case we should say it explicityly instead of just saying "this operation"
            throw new IllegalStateException(errorMsg);
        }
    }

    public boolean isCurrent() {
        return realm().isCurrent(this);
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

    public ContainerLifetimeSetup lifetime() {
        return lifetime;
    }

    /** {@return a new mirror.} */
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

    /** {@return the path of this component} */
    public NamespacePath path() {
        return switch (depth) {
        case 0 -> NamespacePath.ROOT;
        case 1 -> new PackedNamespacePath(name);
        default -> {
            String[] paths = new String[depth];
            ContainerSetup acc = this;
            for (int i = depth - 1; i >= 0; i--) {
                paths[i] = acc.name;
                acc = acc.treeParent;
            }
            yield new PackedNamespacePath(paths);
        }
        };
    }

    public RealmSetup realm() {
        return realm;
    }

    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        throw new UnsupportedOperationException();
    }

    public void setName(String name) {
        this.name = name;
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
            ExtensionSetup extensionParent = treeParent == null ? null : treeParent.useExtensionSetup(extensionClass, requestedByExtension);

            // Create the extension. (This will also add an entry to #extensions)

            extension = new ExtensionSetup(extensionParent, this, extensionClass);
            extension.initialize();
        }
        return extension;
    }

    /** {@inheritDoc} */
    public void named(String newName) {
        // We start by validating the new name of the component
        NameCheck.checkComponentName(newName);

        // Check that this component is still active and the name can be set
        checkIsCurrent();

        String currentName = getName();

        if (newName.equals(currentName)) {
            return;
        }

        // If the name of the component (container) has been set using a wirelet.
        // Any attempt to override will be ignored
        if (isNameInitializedFromWirelet) {
            return;
        }

        // Unless we are the root container. We need to insert this component in the parent container
        if (treeParent != null) {
            if (treeParent.children.putIfAbsent(newName, this) != null) {
                throw new IllegalArgumentException("A component with the specified name '" + newName + "' already exists");
            }
            treeParent.children.remove(currentName);
        }
        setName(newName);
    }
}
