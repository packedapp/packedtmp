/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import app.packed.assembly.Assembly;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.operation.OperationHandle;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.ContainerBeanStore;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionHandle;
import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;
import internal.app.packed.service.MainServiceNamespaceHandle;
import internal.app.packed.util.AbstractNamedTreeNode;
import internal.app.packed.util.accesshelper.BeanAccessHandler;
import internal.app.packed.util.accesshelper.ContainerAccessHandler;

/** The internal configuration of a container. */
public final class ContainerSetup extends AbstractNamedTreeNode<ContainerSetup> implements ComponentSetup, BuildLocalSource {

    /** The application this container is a part of. */
    public final ApplicationSetup application;

    /** The assembly that defines the container. */
    public final AssemblySetup assembly;

    /** The base extension of this container. */
    @Nullable
    private ExtensionSetup baseExtension;

    /** All the beans installed in the container. */
    public final ContainerBeanStore beans = new ContainerBeanStore();

    /** Extensions used by this container. We keep them in a LinkedHashMap so that we can return a deterministic view. */
    // Or maybe extension types are always sorted??
    public final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

    /** The container's handle. */
    @Nullable
    private ContainerHandle<?> handle;

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    // I think we use Wirelet spects instead
    final boolean ignoreRename;

    /** The lifetime the container is a part of. */
    public final ContainerLifetimeSetup lifetime;

    /** The container's service manager. */
    // Maybe replace with ContainerServiceSetup. Where all the logic is.
    private MainServiceNamespaceHandle sm;

    public final ArrayList<Wirelet> unprocessedWirelets;

    public ContainerWireletSpecs wireletSpecs = new ContainerWireletSpecs();

    /**
     * Create a new container.
     *
     * @param installer
     *            the container installer
     * @param assembly
     *            the assembly the defines the container
     */
    private ContainerSetup(PackedContainerInstaller<?> installer, ApplicationSetup application, AssemblySetup assembly) {
        super(installer.parent);
        this.application = requireNonNull(application);
        this.assembly = requireNonNull(assembly);

        if (installer.parent != null) {
            this.lifetime = installer.parent.lifetime;
        } else {
            this.lifetime = new ContainerLifetimeSetup(installer, this, null);
        }

        // If a name has been set using a wirelet, we ignore calls to #named(String)
        this.ignoreRename = installer.nameFromWirelet != null || installer.isFromAssembly;
        this.unprocessedWirelets = installer.unprocessedWirelets;
    }

    /**
     * Returns an immutable set containing any extensions that are disabled for containers created by this driver.
     * <p>
     * When hosting an application, we must merge the parents unsupported extensions and the new guests applications drivers
     * unsupported extensions
     *
     * @return a set of disabled extensions
     */
    public Set<Class<? extends Extension<?>>> bannedExtensions() {
        throw new UnsupportedOperationException();
    }

    /** {@return the base extension for this container.} */
    public ExtensionSetup baseExtension() {
        return requireNonNull(baseExtension);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        List<String> path = new ArrayList<>();
        ContainerSetup currentNode = this;

        while (currentNode != null) {
            path.add(currentNode.name); // Add the current node's name
            currentNode = currentNode.treeParent; // Move to the parent
        }

        Collections.reverse(path);

        return ComponentKind.CONTAINER.pathNew(application.componentPath(), path);
    }

    /** {@return the configuration of the container */
    public ContainerConfiguration configuration() {
        return handle().configuration();
    }

    /** {@return a unmodifiable view of all extension types that are in used in no particular order.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    /** {@inheritDoc} */
    @Override
    public ContainerHandle<?> handle() {
        return requireNonNull(handle);
    }

    /** {@return whether or not the container is the root container in the application.} */
    public boolean isApplicationRoot() {
        return treeParent == null;
    }

    /** {@return whether or not this container is the root container in the assembly} */
    public boolean isAssemblyRoot() {
        // The check for parent == null
        // is because AssemblySetup.container is set after BaseExtension is installed
        // for the root container. And we use this method to test
        return treeParent == null || assembly.container == this;
    }

    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        return extensions.containsKey(extensionClass);
    }

    /** {@return whether or not this container is the root of its lifetime.} */
    public boolean isLifetimeRoot() {
        return this == lifetime.container;
    }

    /**
     * If the container is registered with its own lifetime. This method returns a list of the container's lifetime
     * operations.
     *
     * @return a list of lifetime operations if the container has its own lifetime
     */
    public List<OperationHandle<?>> lifetimeOperations() {
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public BuildLocalMap locals() {
        return application.locals();
    }

    /** {@return a new container mirror.} */
    @Override
    public ContainerMirror mirror() {
        return handle().mirror();
    }

    /** {@return the name of the container} */
    public String name() {
        return requireNonNull(name);
    }

    /**
     * Sets the name of the container
     *
     * @param newName
     *            the new name of the container
     */
    public void named(String newName) {
        // TODO start by checking isConfigurable

        // We start by validating the new name of the component
        NameCheck.checkComponentName(newName);

        // Check that this component is still active and the name can be set

        String currentName = name;

        // If the name of the component (container) has been set using a wirelet.
        // Any attempt to override will be ignored
        if (newName.equals(currentName) || ignoreRename) {
            return;
        }

        // Unless we are the root container. We need to insert or update this container in the parent container
        if (treeParent != null) {
            if (treeParent.treeChildren.putIfAbsent(newName, this) != null) {
                throw new IllegalArgumentException("A container with the specified name '" + newName + "' already exists in the parent");
            }
            treeParent.treeChildren.remove(currentName);
        }
        name = newName;
    }

    private void nameNewContainer(PackedContainerInstaller<?> installer) {
        // Initializes the name of the container
        String nn = installer.nameFromWirelet;

        // Set the name of the container if it was not set by a wirelet
        if (nn == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = installer.name;
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
        }

        String n = nn;
        if (installer.parent != null) {
            HashMap<String, ContainerSetup> c = installer.parent.treeChildren;
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
        this.name = n;
    }

    /** Call {@link Extension#onAssemblyClose()}. */
    public void onAssemblyClose(AuthoritySetup<?> as) {
        for (BeanSetup b : beans) {
            if (b.owner == as) {
                BeanAccessHandler.instance().invokeBeanHandleDoClose(b.handle(), true);
            }
        }

        for (ContainerSetup c : treeChildren.values()) {
            if (assembly == c.assembly) {
                ContainerAccessHandler.instance().invokeContainerHandleDoClose(c.handle());
                c.onAssemblyClose(as);
            }
        }
    }

    public MainServiceNamespaceHandle servicesMain() {
        MainServiceNamespaceHandle s = sm;
        if (s == null) {
            ExtensionHandle<BaseExtension> eh = new PackedExtensionHandle<>(baseExtension());
            s = this.sm = eh.namespaceLazy(MainServiceNamespaceHandle.TEMPLATE);
            s.init(null, this);
        }
        return s;
    }

    /**
     * Returns the extension setup for an extension of the specified type. Installing the extension if it is not already
     * installed.
     * <p>
     * If the requesting extension is non-null. The caller must already have checked that the requested extension is a
     * direct dependency of the requesting extension.
     *
     * @param extensionClass
     *            the type of extension to return an extension setup for
     * @param requestedByExtension
     *            non-null if it is another extension that is requesting the extension
     * @return the extension setup
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     */
    // Any dependencies needed have been checked
    public ExtensionSetup useExtension(Class<? extends Extension<?>> extensionClass, @Nullable ExtensionSetup requestedByExtension) {
        requireNonNull(extensionClass, "extensionClass is null");
        ExtensionSetup extension = extensions.get(extensionClass);

        // We do not use #computeIfAbsent, because extensions might install other extensions while initializing.
        // Which would then fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (extension == null) {
            // is null if requested by user
            if (requestedByExtension == null) {
                // Ny extensions skal installeres indefor Assembly::build

                if (!assembly.isConfigurable()) {
                    throw new IllegalStateException("Extensions cannot be installed outside of Assembly::build");
                }
                // Checks that container is still configurable

                // A user has made a request, that requires an extension to be installed.
                // Check that the realm is still open

                // TODO check that the extensionClass is not banned for users

            } else {
                // An extension has made a request, that requires an extension to be installed.

                // TODO check that the extensionClass is not banned for users

                // TODO Check that the extension user model has not been closed
                if (!requestedByExtension.isConfigurable()) {
                    throw new IllegalStateException();
                }
            }
            extension = ExtensionSetup.newExtension(extensionClass, this, requestedByExtension);
        }
        return extension;
    }

    /**
     * Extracts a bean setup from a bean configuration.
     *
     * @param configuration
     *            the configuration to extract from
     * @return the bean setup
     */
    public static ContainerSetup crack(ContainerConfiguration configuration) {
        return crack(ContainerAccessHandler.instance().getContainerConfigurationHandle(configuration));
    }

    public static ContainerSetup crack(ContainerHandle<?> handle) {
        return ContainerAccessHandler.instance().getContainerHandleContainer(handle);
    }

    public static ContainerSetup crack(ContainerMirror mirror) {
        return crack(ContainerAccessHandler.instance().getContainerMirrorHandle(mirror));
    }

    /**
     * @param <H>
     * @param installer
     *            the installer for the container
     * @param application
     *            the application the container is a part of
     * @param assembly
     *            the assembly the container is a part of
     * @param handleFactory
     *            a handle factory for the container
     * @return
     */
    public static <H extends ContainerHandle<?>> ContainerSetup newContainer(PackedContainerInstaller<?> installer, ApplicationSetup application,
            AssemblySetup assembly) {
        // Cannot reuse an installer
        installer.checkNotUsed();

        // Create the new container using this installer
        ContainerSetup container = installer.install(new ContainerSetup(installer, application, assembly));

        container.nameNewContainer(installer);

        // Create the container's handle.
        container.handle = new ContainerHandle<>(installer);

        // BaseExtension is always present in any container.
        container.baseExtension = ExtensionSetup.newExtension(BaseExtension.class, container, null);

        return container;
    }
}
