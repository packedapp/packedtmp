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
package app.packed.bean;

import app.packed.base.Key;
import app.packed.inject.service.ProvidableBeanConfiguration;

/**
 * The configuration of a container bean.
 * <p>
 * that can serve as basis for actual component configuration types.
 * <p>
 * Component configuration classes do not need to extend this class.
 */

// Og saa alligevel ikke... der er steder hvor vi fx gerne vil tage en ContainerBeanConfiguration.
// Fx de der spi-ting

// extends ServiceBeanConfiguration... (ProvideableBeanConfiguration)
public class ContainerBeanConfiguration<T> extends ProvidableBeanConfiguration<T> {

//    /** A var handle that can update the {@link #configuration()} field in this class. */
//    private static final VarHandle VH_BEAN_SETUP = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "bean", BeanSetup.class);


    /**
     * @param maker
     */
    public ContainerBeanConfiguration(BeanMaker<T> maker) {
        super(maker);
    }

//    /** {@return the container setup instance that we are wrapping.} */
//    private BeanSetup bean() {
//        try {
//            return (BeanSetup) VH_BEAN_SETUP.get((BeanConfiguration) this);
//        } catch (Throwable e) {
//            throw ThrowableUtil.orUndeclared(e);
//        }
//    }


    @Override
    public ContainerBeanConfiguration<T> export() {
        super.export();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    @Override
    protected void onWired() {
        super.onWired();
       // sb.onWired();
    }

    @Override
    public ContainerBeanConfiguration<T> provide() {
        super.provide();
        return this;
    }

    @Override
    public ContainerBeanConfiguration<T> provideAs(Class<? super T> key) {
        super.provideAs(key);
        return this;
    }

    @Override
    public ContainerBeanConfiguration<T> provideAs(Key<? super T> key) {
        super.provideAs(key);
        return this;
    }

}
//
//public <X extends Runnable & Callable<String>> X foo() {
//  return null;
//}