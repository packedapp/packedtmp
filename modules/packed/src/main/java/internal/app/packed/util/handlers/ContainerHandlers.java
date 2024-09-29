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
package internal.app.packed.util.handlers;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerMirror;
import internal.app.packed.container.ContainerSetup;

/**
 *
 */
public final class ContainerHandlers extends Handlers {

    /** A handle that can access ContainerConfiguration#container. */
    private static final VarHandle VH_CONTAINER_CONFIGURATION_TO_SETUP = field(MethodHandles.lookup(), ContainerConfiguration.class,
            "handle", ContainerHandle.class);

    /** A handle that can access {@link ContainerHandleHandle#container}. */
    private static final VarHandle VH_CONTAINER_HANDLE_TO_SETUP = field(MethodHandles.lookup(), ContainerHandle.class, "container",
            ContainerSetup.class);

    /** A handle that can access ContainerMirror#container. */
    private static final VarHandle VH_CONTAINER_MIRROR_TO_HANDLE = field(MethodHandles.lookup(), ContainerMirror.class, "handle",
            ContainerHandle.class);

    public static ContainerHandle<?> getContainerConfigurationHandle(ContainerConfiguration configuration) {
        return (ContainerHandle<?>) VH_CONTAINER_CONFIGURATION_TO_SETUP.get(configuration);
    }

    public static ContainerSetup getContainerHandleContainer(ContainerHandle<?> handle) {
        return (ContainerSetup) VH_CONTAINER_HANDLE_TO_SETUP.get(handle);
    }

    public static ContainerHandle<?> getContainerMirrorHandle(ContainerMirror mirror) {
        return (ContainerHandle<?>) VH_CONTAINER_MIRROR_TO_HANDLE.get(mirror);
    }
}
