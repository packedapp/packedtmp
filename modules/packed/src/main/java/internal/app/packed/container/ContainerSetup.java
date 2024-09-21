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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.ContainerBeanStore;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.BuildLocalMap;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.component.AbstractTreeMirror;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.component.Mirrorable;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.service.ServiceNamespaceHandle;
import internal.app.packed.util.AbstractNamedTreeNode;
import internal.app.packed.util.LookupUtil;

/** The internal configuration of a container. */
public final class ContainerSetup extends AbstractNamedTreeNode<ContainerSetup>
        implements ComponentSetup, ContextualizedElementSetup, Mirrorable<ContainerMirror>, BuildLocalSource {

    /** A handle that can access ContainerConfiguration#container. */
    private static final VarHandle VH_CONTAINER_CONFIGURATION_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), ContainerConfiguration.class,
            "handle", ContainerHandle.class);

    /** A handle that can access {@link ContainerHandleHandle#container}. */
    private static final VarHandle VH_CONTAINER_HANDLE_TO_SETUP = LookupUtil.findVarHandle(MethodHandles.lookup(), ContainerHandle.class, "container",
            ContainerSetup.class);

    /** A handle that can access ContainerMirror#container. */
    private static final VarHandle VH_CONTAINER_MIRROR_TO_HANDLE = LookupUtil.findVarHandle(MethodHandles.lookup(), ContainerMirror.class, "handle",
            ContainerHandle.class);

    /** The application this container is a part of. */
    public final ApplicationSetup application;

    /** The assembly that defines the container. */
    public final AssemblySetup assembly;

    /** All the beans installed in the container. */
    public final ContainerBeanStore beans = new ContainerBeanStore();

    private HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** Extensions used by this container. We keep them in a LinkedHashMap so that we can return a deterministic view. */
    // Or maybe extension types are always sorted??
    public final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

    ContainerHandle<?> handle;

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    final boolean ignoreRename;

    /** The lifetime the container is a part of. */
    public final ContainerLifetimeSetup lifetime;

    /** The container's service manager. */
    private ServiceNamespaceHandle sm;

    public final PackedContainerTemplate template;

    /**
     * Create a new container.
     *
     * @param installer
     *            the container installer
     * @param assembly
     *            the assembly the defines the container
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    ContainerSetup(PackedContainerInstaller installer, ApplicationSetup application, AssemblySetup assembly) {
        super(installer.parent);
        this.application = requireNonNull(application);
        this.assembly = requireNonNull(assembly);
        this.template = installer.template;
        installer.locals.forEach((p, o) -> locals().set((PackedContainerLocal) p, this, o));

        if (installer.template.kind() == PackedContainerKind.PARENT_LIFETIME) {
            this.lifetime = installer.parent.lifetime;
        } else {
            this.lifetime = new ContainerLifetimeSetup(installer, this, null);
        }
        // If a name has been set using a wirelet, we ignore calls to #named(String)
        this.ignoreRename = installer.nameFromWirelet != null;
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
    public BaseExtension base() {
        return (BaseExtension) useExtension(BaseExtension.class, null).instance();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param tags
     * @return
     */
    public ComponentConfiguration componentTag(String[] tags) {
        throw new UnsupportedOperationException();
    }

    public ContainerConfiguration configuration() {
        return handle().configuration();
    }

    /** {@return a unmodifiable view of all extension types that are in used in no particular order.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public ContextSetup findContext(Class<? extends Context<?>> contextClass) {
        Class<? extends Context<?>> cl = ContextInfo.normalize(contextClass);
        return contexts.get(cl);
    }

    /** {@inheritDoc} */
    @Override
    public void forEachContext(BiConsumer<? super Class<? extends Context<?>>, ? super ContextSetup> action) {
        contexts.forEach(action);
    }

    ContainerHandle<?> handle() {
        return requireNonNull(handle);
    }

    /** Call {@link Extension#onAssemblyClose()}. */
    public void invokeOnAssemblyClose(AuthoritySetup as) {
        for (BeanSetup b : beans) {
            if (b.owner == as) {
                b.invokeBeanOnAssemblyClose();
            }
        }
        for (ContainerSetup c : treeChildren.values()) {
            if (assembly == c.assembly) {
                c.invokeOnAssemblyClose(as);
            }
        }
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

    /** {@inheritDoc} */
    public boolean isConfigurable() {
        return assembly.isConfigurable();
    }

    /** {@inheritDoc} */
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        return extensions.containsKey(extensionClass);
    }

    /** {@return whether or not this container is the root of its lifetime.} */
    public boolean isLifetimeRoot() {
        return this == lifetime.container;
    }

//    /** {@return the path of this container} */
//    @Override
//    public OldApplicationPath path() {
//        int depth = node.depth();
//        return switch (depth) {
//        case 0 -> OldApplicationPath.ROOT;
//        case 1 -> new PackedNamespacePath(node.name);
//        default -> {
//            String[] paths = new String[depth];
//            ContainerSetup acc = this;
//            for (int i = depth - 1; i >= 0; i--) {
//                paths[i] = acc.node.name;
//                acc = acc.node.parent;
//            }
//            yield new PackedNamespacePath(paths);
//        }
//        };
//    }

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

    /**
     * Sets the name of the container
     *
     * @param newName
     *            the new name of the container
     */
    public void named(String newName) {
        if (!isConfigurable()) {
            throw new IllegalStateException("The component is no longer configurable");
        }
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
        if (treeParent!= null) {
            if (treeParent.treeChildren.putIfAbsent(newName, this) != null) {
                throw new IllegalArgumentException("A container with the specified name '" + newName + "' already exists in the parent");
            }
            treeParent.treeChildren.remove(currentName);
        }
        name = newName;
    }

    public <T extends Wirelet> WireletSelection<T> selectWireletsUnsafe(Class<T> wireletClass) {

//      WireletWrapper wirelets = extension.container.wirelets;
//      if (wirelets == null || wirelets.unconsumed() == 0) {
//          return WireletSelection.of();
//      }
//
//      return new BuildtimeWireletSelection<>(wirelets, wireletClass);

        throw new UnsupportedOperationException();
    }

    public ServiceNamespaceHandle servicesMain() {
        ServiceNamespaceHandle s = sm;
        if (s == null) {
            s = this.sm = base().namespaceLazy(ServiceNamespaceHandle.NT, "main", inst -> {
                return inst.install(ii -> new ServiceNamespaceHandle(inst, null, this));
            });
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
            extension = ExtensionSetup.install(extensionClass, this, requestedByExtension);
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
        ContainerHandle<?> handle = (ContainerHandle<?>) VH_CONTAINER_CONFIGURATION_TO_SETUP.get(configuration);
        return crack(handle);
    }

    public static ContainerSetup crack(ContainerHandle<?> handle) {
        return (ContainerSetup) VH_CONTAINER_HANDLE_TO_SETUP.get(handle);
    }

    public static ContainerSetup crack(ContainerLocal.Accessor accessor) {
        return switch (accessor) {
        case ContainerConfiguration bc -> crack(bc);
        case ContainerHandle<?> bc -> crack(bc);
        case ContainerMirror bc -> crack(bc);
        };
    }

    public static ContainerSetup crack(ContainerMirror mirror) {
        ContainerHandle<?> handle = (ContainerHandle<?>) VH_CONTAINER_MIRROR_TO_HANDLE.get(mirror);
        return crack(handle);
    }

    /** Implementation of {@link ContainerMirror.OfTree} */
    public static final class PackedContainerTreeMirror extends AbstractTreeMirror<ContainerMirror, ContainerSetup> implements ContainerMirror.OfTree {

        public PackedContainerTreeMirror(ContainerSetup root, @Nullable Predicate<? super ContainerSetup> filter) {
            super(root, filter);
        }
    }
}
