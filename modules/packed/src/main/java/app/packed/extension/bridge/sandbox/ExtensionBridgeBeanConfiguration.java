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
package app.packed.extension.bridge.sandbox;

import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Assembly;
import app.packed.container.ContainerHandle;
import app.packed.container.Wirelet;

/**
 * A bean that creates a new container lifetime.
 */

// Maaske er det her bedre
// newHolderBean(Class<?> beanClass, Bridges);

// newContainerInstaller.useHolderBean(HBConfiguration)

// Vi kan have flere holder beans til


// Jeg tror vi laver en bean...

// Saa resolver vi hver container vi adder op i mod den.

// ExtensionBridge
public class ExtensionBridgeBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ExtensionBridgeBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }
    // addCompanion

    public ContainerHandle linkNewManyContainer(Assembly assembly, Wirelet[] wirelets) {
        throw new UnsupportedOperationException();
    }

    public ContainerHandle newManyContainer(Wirelet[] wirelets) {
        throw new UnsupportedOperationException();
    }
}
