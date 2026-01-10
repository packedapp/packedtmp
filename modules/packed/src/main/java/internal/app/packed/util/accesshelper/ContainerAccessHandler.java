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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerMirror;
import internal.app.packed.container.ContainerSetup;

/**
 * Access helper for ContainerHandle and related classes.
 */
public abstract class ContainerAccessHandler extends AccessHelper {

    private static final Supplier<ContainerAccessHandler> CONSTANT = StableValue.supplier(() -> init(ContainerAccessHandler.class, ContainerHandle.class));

    public static ContainerAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Gets the ContainerHandle from a ContainerConfiguration.
     *
     * @param configuration the configuration
     * @return the container handle
     */
    public abstract ContainerHandle<?> getContainerConfigurationHandle(ContainerConfiguration configuration);

    /**
     * Gets the ContainerSetup from a ContainerHandle.
     *
     * @param handle the handle
     * @return the container setup
     */
    public abstract ContainerSetup getContainerHandleContainer(ContainerHandle<?> handle);

    /**
     * Gets the ContainerHandle from a ContainerMirror.
     *
     * @param mirror the mirror
     * @return the container handle
     */
    public abstract ContainerHandle<?> getContainerMirrorHandle(ContainerMirror mirror);

    /**
     * Invokes the protected doClose method on a ContainerHandle.
     *
     * @param handle the handle
     */
    public abstract void invokeContainerHandleDoClose(ContainerHandle<?> handle);
}
