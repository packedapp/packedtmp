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
package sandbox.extension.bean;

import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanLocal;
import app.packed.bean.BeanMirror;
import app.packed.operation.Op;
import internal.app.packed.bean.PackedBeanBuilder;

/**
 * An installer for installing beans into a container.
 * <p>
 * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
 * are limited.
 *
 * @see BaseExtensionPoint#newBean(BeanKind)
 * @see BaseExtensionPoint#newBeanForExtension(BeanKind, app.packed.extension.ExtensionPoint.UseSite)
 */
public sealed interface BeanBuilder permits PackedBeanBuilder {

    /**
     * Installs the bean using the specified class as the bean source.
     *
     * @param <T>
     *            the type of bean
     * @param beanClass
     *            the bean class
     * @return a bean handle representing the installed bean
     *
     * @see app.packed.bean.BeanSourceKind#CLASS
     */
    <T> BeanHandle<T> install(Class<T> beanClass);

    <T> BeanHandle<T> install(Op<T> operation);

    // These things can never be multi
    <T> BeanHandle<T> installIfAbsent(Class<T> beanClass, Consumer<? super BeanHandle<T>> onInstall);

    // instance = introspected bean
    // constant = non-introspected bean
    <T> BeanHandle<T> installInstance(T instance);

    BeanBuilder namePrefix(String prefix);

    /**
     * Sets the value of the specified bean local for the bean being built.
     *
     * @param <T>
     *            the type of value the bean local holds
     * @param local
     *            the bean local to set
     * @param value
     *            the value of the local
     * @return this builder
     */
    <T> BeanBuilder setLocal(BeanLocal<T> local, T value);

    /**
     * Sets a supplier that creates a special bean mirror instead of the generic {@code BeanMirror} when a mirror is needed.
     *
     * @param supplier
     *            the supplier used to create the bean mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     */
    BeanBuilder specializeMirror(Supplier<? extends BeanMirror> supplier);
}