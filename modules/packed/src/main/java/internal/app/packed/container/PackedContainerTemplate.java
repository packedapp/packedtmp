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

import java.util.function.Function;

import app.packed.container.ContainerHandle;
import app.packed.container.ContainerInstaller;

/** Implementation of {@link ContainerTemplate}. */
public record PackedContainerTemplate<H extends ContainerHandle<?>>(PackedContainerKind kind, Class<?> holderClass, Class<?> resultType, boolean isManaged) {

    /**
     * A base template for a container that has the same lifetime as its parent container.
     * <p>
     * This template does supports any {@link #lifetimeOperations() lifetime operations} as the container is automatically
     * created when the parent container is created.
     * <p>
     * This template does not support carrier objects. (or do we??)
     */
    // public static final PackedContainerTemplate<?> DEFAULT = new
    // PackedContainerTemplate<>(PackedContainerKind.FROM_CONTAINER);

    /**
     * A container template representing a container that exists solely within a single entry point operation.
     * <p>
     * The container is created. The method is executed. And the container is shutdown again
     * <p>
     * A container lifetime created using this template must have registered at least one entry point. Otherwise an
     * {@link app.packed.extension.InternalExtensionException} is thrown.
     * <p>
     * TODO we need a method where we can set a supplier that is executed. It is typically a user error. The specified
     * assembly must hava at least one method that schedules shit
     *
     * @see app.packed.extension.BeanElement.BeanMethod#newLifetimeOperation(ContainerHandle)
     * @see app.packed.extension.bean.BeanTemplate#Z_FROM_OPERATION
     **/
    // public static final PackedContainerTemplate<?> GATEWAY = new PackedContainerTemplate<>(PackedContainerKind.GATEWAY);

    // Cannot have managed on unmanaged
    public static final PackedContainerTemplate<?> MANAGED = new PackedContainerTemplate<>(PackedContainerKind.MANAGED);

    // Carefull with Unmanaged on Managed
    public static final PackedContainerTemplate<?> UNMANAGED = new PackedContainerTemplate<>(PackedContainerKind.UNMANAGED);

    public PackedContainerTemplate(PackedContainerKind kind) {
        this(kind, void.class, void.class);
    }

    private PackedContainerTemplate(PackedContainerKind kind, Class<?> holderClass, Class<?> resultType) {
        this(kind, holderClass, resultType, false);
    }

    @SuppressWarnings("unchecked")
    public Function<? super ContainerInstaller<?>, H> handleFactory() {
        return i -> (H) new ContainerHandle<>(i);
    }

    @Override
    public boolean isManaged() {
        return kind != PackedContainerKind.UNMANAGED;
    }
}
