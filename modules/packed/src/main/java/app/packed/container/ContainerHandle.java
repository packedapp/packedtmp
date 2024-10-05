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
package app.packed.container;

import java.util.List;
import java.util.Set;

import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerInstaller;
import sandbox.extension.context.ContextSpanKind;

/**
 * A container handle is a build-time reference to an installed container. They are created by the framework when an
 * extension installs a container on behalf of the user (or an another extension).
 */
public non-sealed class ContainerHandle<C extends ContainerConfiguration> extends ComponentHandle implements ContainerBuildLocal.Accessor {

    /** The lazy generated container configuration. */
    private C configuration;

    /** The handle's container */
    final ContainerSetup container;

    /** Whether or not the container if configurable. {@link #doClose()} marks it as non-configurable. */
    private boolean isConfigurable = true;

    /** The lazy generated container mirror. */
    private ContainerMirror mirror;


    /**
     * Creates a new container handle.
     *
     *
     * @param installer
     *            the installer for the container
     */
    public ContainerHandle(ContainerInstaller<?> installer) {
        this.container = ((PackedContainerInstaller<?>) installer).toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return container.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final void componentTag(String... tags) {
        checkIsConfigurable();
        container.application.componentTags.addComponentTags(container, tags);
    }

    /** { @return the user exposed configuration of the bean} */
    public final C configuration() {
        C c = configuration;
        if (c == null) {
            c = configuration = newContainerConfiguration();
        }
        return c;
    }

    final void doClose() {
        onClose();
        isConfigurable = false;
    }

    /**
     * {@return an unmodifiable view of the extensions that are currently used by this container.}
     *
     * @see #use(Class)
     * @see BaseAssembly#extensionsTypes()
     * @see ContainerMirror#extensionsTypes()
     */
    public final Set<Class<? extends Extension<?>>> extensionTypes() {
        return container.extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return isConfigurable;
    }

    /**
     * Returns whether or not the specified extension is used by this extension, other extensions, or user code in the same
     * container as this extension.
     *
     * @param extensionType
     *            the extension type to test
     * @return {@code true} if the extension is currently in use, otherwise {@code false}
     * @implNote The framework does not perform detailed tracking on which extensions use other extensions. As a consequence
     *           it cannot give a more detailed answer about who is using a particular extension
     * @see ContainerConfiguration#isExtensionUsed(Class)
     * @see ContainerMirror#isExtensionUsed(Class)
     */
    public final boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return container.isExtensionUsed(extensionType);
    }

    /**
     * This method returns a list of the container's lifetime operations.
     * <p>
     * If the lifetime of the container container cannot be explicitly controlled, for example, if it is a child container.
     * The returned list is empty.
     *
     * @return a list of lifetime operations of this container.
     */
    public final List<OperationHandle<?>> lifetimeOperations() {
        return container.lifetimeOperations();
    }

    /**
     * {@inheritDoc}
     *
     * @see #newContainerMirror()
     */
    @Override
    public final ContainerMirror mirror() {
        ContainerMirror m = mirror;
        if (m == null) {
            m = mirror = newContainerMirror();
        }
        return m;
    }

    public final String name() {
        return container.name();
    }

    @SuppressWarnings("unchecked")
    protected C newContainerConfiguration() {
        return (C) new ContainerConfiguration(this);
    }

    /**
     * This method may be overridden a subclass to provide a another implementation of container mirror, than the default
     * {@link ContainerMirror}.
     * <p>
     * This method will never be invoked more than once by the framework. The result will be cached and returned from
     * subsequent calls to {@link #mirror}.
     *
     * @return create a new container mirror
     *
     * @see #mirror()
     */
    protected ContainerMirror newContainerMirror() {
        return new ContainerMirror(this);
    }

    protected void onClose() {}

    /** {@inheritDoc} */
    @Override
    public String toString() {
        // Should ComponentPath
        return "Container: " + container.name();
    }
}

interface ZandboxHandle {
    // ditch beanBlass, and just make sure there is a bean that can do it
    default ZandboxHandle zContextFromBean(Class<?> beanClass, ContextTemplate template, ContextSpanKind span) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}
     *
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a container handle representing the linked container
     */
//    default ZandboxHandle zErrorHandle(ErrorHandler h) {
//        return this;
//    }

    // The application will fail to build if the installing extension
    // is not used by. Is only applicable for new(Assembly)
    // Maaske er det fint bare en wirelet der kan tage en custom besked?
    // Smider den paa templaten
    // default Zandbox zBuildAndRequiresThisExtension(Assembly assembly, Wirelet... wirelets) {
    // throw new UnsupportedOperationException();
    // }

}
