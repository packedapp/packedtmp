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
package app.packed.application;

import java.util.Set;
import java.util.function.Function;

import app.packed.container.ContainerTemplate;
import app.packed.operation.Op;
import internal.app.packed.application.PackedApplicationTemplate;

/**
 * A template for creating new applications. It is typically only used by {@link app.packed.extension.Extension
 * extensions} and normal users will rarely have any use for it.
 *
 * @param <H>
 *            the type of application handles the template creates
 */
public sealed interface ApplicationTemplate<H extends ApplicationHandle<?, ?>> permits PackedApplicationTemplate {

    /** {@return the initial component tags for the application} */
    Set<String> componentTags();

    /** {@return the handle class that was specified when creating the template} */
    Class<? super H> handleClass();

    /** {@return whether this template represents a managed or unmanaged application} */
    default boolean isManaged() {
        return rootContainer().isManaged();
    }

    /** {@return the template for the root container of the application} */
    ContainerTemplate<?> rootContainer();

    /**
     * Add the specified tag(s) to the application.
     *
     * @param tags
     *            the tag(s) to add
     * @return this configurator
     * @see ApplicationMirror#componentTags()
     * @see ApplicationHandle#componentTag(String...)
     * @see ApplicationConfiguration#componentTag(String...)
     */
    ApplicationTemplate<H> withComponentTags(String... tags);

    /**
     * Configures the container template that should be used for the root container of the application.
     *
     * @param template
     *            a template for the root container of the application
     * @return this configurator
     * @see #rootContainer(Consumer)
     */
    ApplicationTemplate<H> withRootContainer(ContainerTemplate<?> template);

//    /**
//     * Create a new application template, that will use
//     *
//     * <p>
//     * This method calls {@link #of(Class, Consumer, Class, Function)} with
//     * {@code ApplicationHandle.class, ApplicationHandle::new} as respectively the handler class and the handler factory.
//     *
//     * @param <A>
//     *            the type of host class that are returned when launching the application.
//     * @param hostClass
//     * @param configurator
//     * @return the new template
//     *
//     * @throws IllegalStateException
//     *             if attempting to create an application template without {@link Configurator#container(Consumer) setting}
//     *             a container template for the application's root container
//     */

    static <I> ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> ofManaged(Class<I> hostClass) {
        return ofManaged(hostClass, ApplicationHandle.class, ApplicationHandle::new);
    }

    static <I, H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> ofManaged(Class<I> hostClass, Class<? super H> handleClass,
            Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
        return new PackedApplicationTemplate<H>(hostClass, null, handleClass, handleFactory).withRootContainer(ContainerTemplate.MANAGED);
    }

    static <I> ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> ofManaged(Op<I> hostOp) {
        return ofManaged(hostOp, ApplicationHandle.class, ApplicationHandle::new);
    }

    static <I, H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> ofManaged(Op<I> hostOp, Class<? super H> handleClass,
            Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
        Class<?> type = hostOp.type().returnRawType();
        return new PackedApplicationTemplate<>(type, hostOp, handleClass, handleFactory).withRootContainer(ContainerTemplate.MANAGED);
    }

    static <I> ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> ofUnmanaged(Class<I> hostClass) {
        return ofManaged(hostClass, ApplicationHandle.class, ApplicationHandle::new);
    }

    static <I, H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> ofUnmanaged(Class<I> hostClass, Class<? super H> handleClass,
            Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
        return new PackedApplicationTemplate<H>(hostClass, null, handleClass, handleFactory).withRootContainer(ContainerTemplate.UNMANAGED);
    }

    static <I> ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> ofUnmanaged(Op<I> hostOp) {
        return ofUnmanaged(hostOp, ApplicationHandle.class, ApplicationHandle::new);
    }

    static <I, H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> ofUnmanaged(Op<I> hostOp, Class<? super H> handleClass,
            Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
        Class<?> type = hostOp.type().returnRawType();
        return new PackedApplicationTemplate<>(type, hostOp, handleClass, handleFactory).withRootContainer(ContainerTemplate.UNMANAGED);
    }

    // <T> Configurator setLocal(ApplicationBuildLocal<T> local, T value);
}
