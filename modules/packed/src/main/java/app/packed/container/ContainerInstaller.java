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

import java.util.function.Supplier;

import app.packed.bean.InstanceBeanConfiguration;
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

    default ContainerInstaller errorHandle(ErrorHandler h) {
        return this;
    }

    /**
     * Creates a new container using the specified assembly
     * <p>
     * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}. Any
     * configuration of the new container must be done prior to calling this method.
     *
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return a container handle representing the linked container
     */
    ContainerHandle install(Assembly assembly, Wirelet... wirelets);

    /**
     * Installs a new container with the .
     *
     * @param wirelets
     *            optional wirelets
     * @return a container handle for the newly installed container
     */
    ContainerHandle install(Wirelet... wirelets);

    default ContainerInstaller named(String name) {
        throw new UnsupportedOperationException();
    }

    // The application will fail to build if the installing extension
    // is not used by. Is only applicable for new(Assembly)
    default ContainerInstaller requireUseOfExtension() {
        throw new UnsupportedOperationException();
    }

    default ContainerInstaller requireUseOfExtension(String errorMessage) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a supplier that creates a special bean mirror instead of the generic {@code BeanMirror} when requested.
     *
     * @param supplier
     *            the supplier used to create the bean mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     */
    default void specializeMirror(Supplier<? extends ContainerMirror> supplier) {
        throw new UnsupportedOperationException();
    }

    // Does not work with assembly
    // useThis
    /**
     * <p>
     * This method cannot be used together with {@link #install(Assembly, Wirelet...)}.
     *
     * @return
     */
    default ContainerInstaller useThisExtension() {
        throw new UnsupportedOperationException();
    }

    // Only Managed-Operation does not require a wrapper
    default ContainerInstaller wrapIn(InstanceBeanConfiguration<?> wrapperBeanConfiguration) {
        // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum supportere det
        // Hvis vi vil dele den...

        // Det betyder ogsaa vi skal lave en wrapper bean alene
        return null;
    }
}