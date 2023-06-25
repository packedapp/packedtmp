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
import app.packed.container.Wirelet;
import app.packed.util.Key;
import sandbox.extension.bean.BeanHandle;

/**
 *
 */
public final class ContainerCarrierBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     *
     * @param handle
     *            the bean handle
     */
    // TODO Package private I think
    public ContainerCarrierBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    // All guest will have these wirelets
    public ContainerCarrierBeanConfiguration<T> addWirelets(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <K> ContainerCarrierBeanConfiguration<T> overrideService(Class<K> key, K instance) {
        super.overrideService(key, instance);
        return this;
    }

    @Override
    public <K> ContainerCarrierBeanConfiguration<T> overrideService(Key<K> key, K instance) {
        super.overrideService(key, instance);
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
    public <S> ContainerHandleBuilder carrierProvideConstant(Class<S> key, S constant) {
        return carrierProvideConstant(Key.of(key), constant);
    }

    /**
     * @see FromLifetimeChannel
     */
    public <S> ContainerHandleBuilder carrierProvideConstant(Key<S> key, S constant) {
        throw new UnsupportedOperationException();
    }
}

//// Nah det er vel bare at capture exceptionen fra lifetime operationen...
//public ContainerGuestConfiguration<T> onFailedInstallation(Consumer<T> t) {
//  return this;
//}

// Guest ->