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

import java.util.List;
import java.util.Set;

import app.packed.application.ApplicationPath;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.Extension;
import app.packed.extension.container.ContainerHandle;
import app.packed.extension.operation.OperationHandle;

/** Implementation of {@link ContainerHandle}. */
public record PackedContainerHandle(ContainerSetup container) implements ContainerHandle {

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

    /**
     * Returns whether or not the bean is still configurable.
     * <p>
     * If an assembly was used to create the container. The handle is never configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    @Override
    public boolean isConfigurable() {
        return container.assembly.isConfigurable();
    }

    // Hmm, skal vi have selve handles'ene?
    // Jo det skal vi faktisk nok...

    /**
     * If the container is registered with its own lifetime. This method returns a list of the container's lifetime
     * operations.
     *
     * @return a list of lifetime operations if the container has its own lifetime
     */
    @Override
    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    /**
     * Sets the name of the container.
     *
     * @param name
     *
     * @throws IllegalStateException
     *             if the container is no long configurable
     *
     * @see ContainerConfiguration#named(String)
     * @see ContainerInstaller#named(String)
     */
    public void named(String name) {
        container.named(name);
    }


    /**
     * @param errorHandler
     *
     * @see ContainerInstaller#errorHandle(ErrorHandler)
     *
     * @throws IllegalStateException
     *             if the container is no long configurable
     */
    // Denneer primaert taenkt for brugeren, hvis fx usereren skal have lov at besteem error handleren
    // En wirelet er alternativet
    public void setErrorHandler(ErrorHandler errorHandler) {
        checkIsConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return container.isExtensionUsed(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension<?>>> extensionTypes() {
        return container.extensionTypes();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationPath path() {
        return container.path();
    }
}
