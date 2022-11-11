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
import java.util.Set;
import java.util.function.Supplier;

import app.packed.application.NamespacePath;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.framework.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.service.OldServiceResolver;
import internal.app.packed.service.ServiceManager;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of a container. */
public final class ContainerSetup extends AbstractTreeNode<ContainerSetup> {

    /** A MethodHandle for invoking {@link ContainerMirror#initialize(ContainerSetup)}. */
    private static final MethodHandle MH_CONTAINER_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ContainerMirror.class,
            "initialize", void.class, ContainerSetup.class);

    /** The application this container is a part of. */
    public final ApplicationSetup application;

    /** The assembly from where this container was defined. */
    public final AssemblySetup assembly;

    /** A map of all non-void bean classes. Used for controlling non-multi-install beans. */
    public final HashMap<Class<?>, Object> beanClassMap = new HashMap<>();

    /** All beans installed in a container is maintained in a linked list, this field pointing to the first bean. */
    @Nullable
    public BeanSetup beanFirst;

    /** All beans installed in a container is maintained in a linked list, this field pointing to the last bean. */
    @Nullable
    public BeanSetup beanLast;

    /** Maintains unique names for beans and child containers. */
    public final HashMap<String, Object> children = new HashMap<>();

    /** The depth of the component in the application tree. */
    public final int depth; // maintain in InsertionTree?

    /** Extensions used by this container. We keep them in a LinkedHashMap so that we can return a deterministic view. */
    // Or maybe extension types are always sorted??
    public final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

    /** The container's injection manager. */
    public final OldServiceResolver injectionManager = new OldServiceResolver();

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    boolean isNameInitializedFromWirelet;

    /** The lifetime the container is a part of. */
    public final ContainerLifetimeSetup lifetime;

    /** The name of the container. */
    public String name;

    /** The container's service manager. */
    public final ServiceManager sm;

    /** Supplies a mirror for the container. */
    public Supplier<? extends ContainerMirror> specializedMirror;

    /** Wirelets that were specified when creating the component. */
    // As an alternative non-final, and then nulled out whenever the last wirelet is consumed
    @Nullable
    public final WireletWrapper wirelets;

    /**
     * Create a new container setup.
     * 
     * @param application
     *            the application this container is a part of
     * @param assembly
     *            the assembly the container is defined in
     * @param parent
     *            any parent container
     * @param wirelets
     *            optional wirelets
     */
    public ContainerSetup(ApplicationSetup application, AssemblySetup assembly, @Nullable ContainerSetup parent, Wirelet[] wirelets) {
        super(parent);

        this.application = requireNonNull(application);
        this.assembly = assembly;
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
            initializeNameWithPrefix(n);
        }
        assert name != null;
    }

    /** {@return a unmodifiable view of all extension types that are in used in no particular order.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
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

    /**
     * Returns whether or not the specified extension class is used.
     * 
     * @param extensionClass
     *            the extension to test
     * @return true if the specified extension type is used, otherwise false
     * @see ContainerConfiguration#isExtensionUsed(Class)
     * @see ContainerMirror#isUsed(Class)
     */
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        return extensions.containsKey(extensionClass);
    }

    /** {@return a new container mirror.} */
    public ContainerMirror mirror() {
        ContainerMirror mirror = ClassUtil.mirrorHelper(ContainerMirror.class, ContainerMirror::new, specializedMirror);

        // Initialize ContainerMirror by calling ContainerMirror#initialize(ContainerSetup)
        try {
            MH_CONTAINER_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public void named(String newName) {
        // We start by validating the new name of the component
        NameCheck.checkComponentName(newName);

        // Check that this component is still active and the name can be set

        String currentName = name;

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
        name = newName;
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

    public <T extends Wirelet> WireletSelection<T> selectWirelets(Class<T> wireletClass) {
        throw new UnsupportedOperationException();
    }

    /**
     * If an extension of the specified type has not already been installed, installs it. Returns the extension's context.
     * <p>
     * If the requesting extension is non-null. The calller must already have checked that the requested extension is a
     * direct dependency of the requesting extension.
     * 
     * @param extensionClass
     *            the type of extension to return a setup for
     * @param requestedByExtension
     *            non-null if it is another extension that is requesting the extension
     * @return the extension setup
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     */
    // Any dependencies needed have been checked
    public ExtensionSetup safeUseExtensionSetup(Class<? extends Extension<?>> extensionClass, @Nullable ExtensionSetup requestedByExtension) {
        requireNonNull(extensionClass, "extensionClass is null");
        ExtensionSetup extension = extensions.get(extensionClass);

        // We do not use #computeIfAbsent, because extensions might install other extensions when initializing.
        // Which would then fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (extension == null) {
            // Ny extensions skal installeres indefor Assembly::build

            if (assembly.isClosed()) {
                throw new IllegalStateException("New extensions cannot be installed outside of Assembly::build");
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
            ExtensionSetup extensionParent = treeParent == null ? null : treeParent.safeUseExtensionSetup(extensionClass, requestedByExtension);

            // Create the extension. (This will also add an entry to #extensions)

            extension = new ExtensionSetup(extensionParent, this, extensionClass);
            extension.initialize();
        }
        return extension;
    }
}
