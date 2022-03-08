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

/**
 * A configuration of a container bean.
 */
// Tror ikke den kan noget PBC ikke kan.
// Men vi kan kraeve den som parameter nogle steder.
public class ContainerBeanConfiguration<T> extends ProvidableBeanConfiguration<T> {

    /**
     * @param driver
     *            the bean driver to use for creating the bean
     */
    public ContainerBeanConfiguration(BeanDriver<T> driver) {
        super(driver);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public ContainerBeanConfiguration<T> provide() {
        super.provide();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerBeanConfiguration<T> provideAs(Class<? super T> key) {
        super.provideAs(key);
        return this;
    }

    /** {@inheritDoc} */
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