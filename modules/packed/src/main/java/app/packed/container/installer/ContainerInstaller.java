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
package app.packed.container.installer;

import java.util.function.Supplier;

import app.packed.container.Assembly;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.errorhandling.ErrorHandler;

/**
 *
 */
public interface ContainerInstaller {
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
    ContainerHandle link(Assembly assembly, Wirelet... wirelets);

    ContainerHandle newContainer(Wirelet... wirelets);

    ContainerInstaller allowRuntimeWirelets();

    ContainerInstaller errorHandle(ErrorHandler h);

    // Does not work with assembly
    // useThis
    ContainerInstaller useThisExtension();

    ContainerInstaller named(String name);

    // The application will fail to build if the installing extension
    // is not used by. Is only applicable for new(Assembly)
    ContainerInstaller requireUseOfExtension();

    ContainerInstaller requireUseOfExtension(String errorMessage);

    /**
     * Sets a supplier that creates a special bean mirror instead of the generic {@code BeanMirror} when requested.
     *
     * @param supplier
     *            the supplier used to create the bean mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     */
    void specializeMirror(Supplier<? extends ContainerMirror> supplier);
}
