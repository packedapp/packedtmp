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

import app.packed.container.ContainerHandle;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;

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
    @Override
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

    /** {@inheritDoc} */
    @Override
    public void named(String name) {
        container.named(name);
    }

    /** {@inheritDoc} */
    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
        checkIsConfigurable();
    }
}
