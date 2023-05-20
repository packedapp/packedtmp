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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import app.packed.application.OldApplicationPath;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.container.WireletSelection;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedElementSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.service.ServiceManager;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.PackedNamespacePath;
import internal.app.packed.util.types.ClassUtil;

/** The internal configuration of a container. */
public final class ContainerSetup extends AbstractTreeNode<ContainerSetup> implements ContextualizedElementSetup {

    /** A magic initializer for {@link ContainerMirror}. */
    public static final MagicInitializer<ContainerSetup> MIRROR_INITIALIZER = MagicInitializer.of(ContainerMirror.class);

    /** The application this container is a part of. */
    public final ApplicationSetup application;

    /** The assembly that defines the container. */
    public final AssemblySetup assembly;

    /** All the beans installed in the container. */
    public final ContainerBeanStore beans = new ContainerBeanStore();

    /** Maintains unique names for beans and child containers. */
    public final HashMap<String, ContainerSetup> children = new HashMap<>();

    private HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** Extensions used by this container. We keep them in a LinkedHashMap so that we can return a deterministic view. */
    // Or maybe extension types are always sorted??
    public final LinkedHashMap<Class<? extends Extension<?>>, ExtensionSetup> extensions = new LinkedHashMap<>();

    /**
     * Whether or not the name has been initialized via a wirelet, in which case calls to {@link #named(String)} are
     * ignored.
     */
    final boolean ignoreRename;

    /** The lifetime the container is a part of. */
    public final ContainerLifetimeSetup lifetime;

    /** Supplies a mirror for the container. */
    private final Supplier<? extends ContainerMirror> mirrorSupplier;

    /** The name of the container. */
    public String name;

    /** The container's service manager. */
    public final ServiceManager sm;

    /**
     * Create a new container.
     *
     * @param builder
     *            the container builder
     * @param assembly
     *            the assembly the defines the container
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    ContainerSetup(PackedContainerBuilder builder, ApplicationSetup application, AssemblySetup assembly) {
        super(builder.parent);
        this.application = requireNonNull(application);
        this.assembly = requireNonNull(assembly);
        this.mirrorSupplier = builder.containerMirrorSupplier;
        this.name = builder.name;

        builder.locals.forEach((p, o) -> p.locals(this).set((PackedLocal) p, this, o));

        if (builder.template.kind() == PackedContainerKind.PARENT_LIFETIME) {
            this.lifetime = treeParent.lifetime;
        } else {
            this.lifetime = new ContainerLifetimeSetup(builder, this, null);
        }
        this.sm = new ServiceManager(null, this);
        // If a name has been set using a wirelet, we ignore calls to #named(String)
        this.ignoreRename = builder.nameFromWirelet != null;

        // TODO copy Locals from builder

    }

    /** {@return the base extension for this container.} */
    public BaseExtension base() {
        return (BaseExtension) useExtension(BaseExtension.class, null).instance();
    }

    /** {@return a unmodifiable view of all extension types that are in used in no particular order.} */
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    @Override
    @Nullable
    public ContextSetup findContext(Class<? extends Context<?>> contextClass) {
        Class<? extends Context<?>> cl = ContextInfo.normalize(contextClass);
        return contexts.get(cl);
    }

    @Override
    public void forEachContext(BiConsumer<? super Class<? extends Context<?>>, ? super ContextSetup> action) {
        contexts.forEach(action);
    }

    /** {@return whether or not the container is the root container in the application.} */
    public boolean isApplicationRoot() {
        return treeParent == null;
    }

    public boolean isAssemblyRoot() {
        // The check for treeParent == null
        // is because AssemblySetup.container is set after BaseExtension is installed
        // for the root container. And we use this method to test
        return treeParent == null || assembly.container == this;
    }

    /**
     * Returns whether or not the specified extension class is used.
     *
     * @param extensionClass
     *            the extension to test
     * @return true if the specified extension type is used, otherwise false
     * @see ContainerConfiguration#isExtensionUsed(Class)
     * @see ContainerMirror#isExtensionUsed(Class)
     */
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionClass) {
        requireNonNull(extensionClass, "extensionClass is null");
        return extensions.containsKey(extensionClass);
    }

    /** {@return whether or not this container is the root of its lifetime.} */
    public boolean isLifetimeRoot() {
        return this == lifetime.container;
    }

    /** {@return a new container mirror.} */
    @Override
    public ContainerMirror mirror() {
        return MIRROR_INITIALIZER.run(() -> ClassUtil.newMirror(ContainerMirror.class, ContainerMirror::new, mirrorSupplier), this);
    }

    /**
     * Sets the name of the container
     *
     * @param newName
     *            the new name of the container
     */
    public void named(String newName) {
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
            if (treeParent.children.putIfAbsent(newName, this) != null) {
                throw new IllegalArgumentException("A bean or container with the specified name '" + newName + "' already exists");
            }
            treeParent.children.remove(currentName);
        }
        name = newName;
    }

    /** {@return the path of this container} */
    public OldApplicationPath path() {
        int depth = depth();
        return switch (depth) {
        case 0 -> OldApplicationPath.ROOT;
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
//      WireletWrapper wirelets = extension.container.wirelets;
//      if (wirelets == null || wirelets.unconsumed() == 0) {
//          return WireletSelection.of();
//      }
//
//      return new BuildtimeWireletSelection<>(wirelets, wireletClass);

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
}
