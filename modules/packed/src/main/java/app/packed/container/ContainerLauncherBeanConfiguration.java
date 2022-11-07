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

import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;

/**
 * A bean that creates a new container lifetime.
 */
public class ContainerLauncherBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    ContainerLauncherBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }
    // addCompanion 
    
    public ContainerHandle linkNewManyContainer(Assembly assembly, Wirelet[] wirelets, ContainerHandle.InstallOption... options) {
        throw new UnsupportedOperationException();
    }
    
    public ContainerHandle newManyContainer(Wirelet[] wirelets, ContainerHandle.InstallOption... options) {
        throw new UnsupportedOperationException();
    } 
}
