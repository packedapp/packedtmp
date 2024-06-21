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
package sandbox.extension.container;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.util.Key;
import sandbox.extension.bean.BeanTemplate;

/**
 * A container host bean is simply a bean that hosts another container (possible the root container in an application)
 */
// Was ContainerCarrierBeanConfiguration????

// Holds a single container instance? Or can have multiple instances?
// Maybe a method to enable multiple instances? But we need to bean

// Single Static Container
// Multiple Static Container IDK

// There can be multiple guest per guest adaptors
public final class ComponentGuestAdaptorBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     */
    public ComponentGuestAdaptorBeanConfiguration(BeanTemplate.Installer handle) {
        super(handle);
    }

    public Class<T> holderClass() {
        throw new UnsupportedOperationException();
    }

    // All guest will have these wirelets
//    public ContainerHostBeanConfiguration<T> addWirelets(Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public <K> ComponentGuestAdaptorBeanConfiguration<T> bindInstance(Class<K> key, K instance) {
        super.bindInstance(key, instance);
        return this;
    }

    @Override
    public <K> ComponentGuestAdaptorBeanConfiguration<T> bindInstance(Key<K> key, K instance) {
        super.bindInstance(key, instance);
        return this;
    }

    /**
     * @param <T>
     * @param key
     * @param arg
     * @return
     *
     * @see ExtensionLink#ofConstant(Class, Object)
     */
    // Er det til guesten???? Eller configuration af hosten???
    public <S> ContainerTemplate.Installer carrierProvideConstant(Class<S> key, S constant) {
        return carrierProvideConstant(Key.of(key), constant);
    }

    /**
     * @see FromLifetimeChannel
     */
    public <S> ContainerTemplate.Installer carrierProvideConstant(Key<S> key, S constant) {
        throw new UnsupportedOperationException();
    }
}

//// Nah det er vel bare at capture exceptionen fra lifetime operationen...
//public ContainerGuestConfiguration<T> onFailedInstallation(Consumer<T> t) {
//  return this;
//}

// Guest ->