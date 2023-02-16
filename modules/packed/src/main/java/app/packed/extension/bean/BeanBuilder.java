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
package app.packed.extension.bean;

import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanMirror;
import app.packed.extension.BeanLocal;
import app.packed.operation.Op;
import internal.app.packed.bean.PackedBeanInstaller;

/**
 * An installer for installing beans into a container.
 * <p>
 * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
 * are limited.
 *
 * @see BaseExtensionPoint#newBean(BeanKind)
 * @see BaseExtensionPoint#newBeanForExtension(BeanKind, app.packed.extension.ExtensionPoint.UseSite)
 */
// Maybe put it back on BEP. If we get OperationInstaller
// Maybe Builder after all... Alle ved hvad en builder er
// BeanBuilder paa BEP
public sealed interface BeanBuilder permits PackedBeanInstaller {

    /**
     * Installs the bean using the specified class as the bean source.
     *
     * @param <T>
     *            the
     * @param beanClass
     * @return a bean handle representing the installed bean
     */
    <T> BeanHandle<T> install(Class<T> beanClass);

    <T> BeanHandle<T> install(Op<T> operation);

    <T> BeanHandle<T> installIfAbsent(Class<T> beanClass, Consumer<? super BeanHandle<T>> onInstall);

    // instance = introspected bean
    // constant = non-introspected bean
    <T> BeanHandle<T> installInstance(T instance);

    BeanHandle<Void> installWithoutSource();

    /**
     * An option that allows for a special bean introspector to be used when introspecting the bean for the extension.
     * Normally, the runtime would call {@link Extension#newBeanIntrospector} to obtain an introspector for the registering
     * extension.
     *
     * @param introspector
     *            the introspector to use
     * @return the option
     * @see Extension#newBeanIntrospector
     */
    // Den er langt mindre brugbar end foerst antaget. Fordi vi bliver noedt til at processere alle
    // annotering og give gode fejlmeddelse for hvorfor man ikke kan benytte dem

    // Hvad skal vi helt praecis goere her...
    // Vi bliver noedt til at vide hvilke kontekts der er...
    // Saa vi skal vel have OperationTemplates

    //// Hvad med @Get som laver en bean...
    //// Det er vel operationen der laver den...

    // No Lifetime, Container, Static, Functional, Static

    // Operational -> A bean that is instantiated and lives for the duration of an operation

    // MANYTONE -> Controlled

    /**
     * Allows multiple beans of the same type in a container.
     * <p>
     * By default, a container only allows a single bean of particular type if non-void.
     *
     * @return this builder
     * @throws UnsupportedOperationException
     *             if bean kind is {@link BeanKind#FUNCTIONAL} or {@link BeanKind#STATIC}
     */
    BeanBuilder multi();

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

    // A bean that is created per operation.
    // Obvious manyton, but should we have own kind?
    // I actually think so because, because for now it always requires manyton

    // Some questions, do we support @Schedule? Or anything like it?
    // I don't think we need to set up the support for it by default. Only if used
    // So overhead is not needed

    // But I think those annotations that make sense are always "callback" extensions
    // From other threads
    // Single threaded vs multi-threaded
    // If we are single threaded it is obviously always only the request method
    // If we are multi threaded we create own little "world"
    // I think that is the difference, between the two

    // Maybe bean is always single threaded.
    // And container is always multi threaded

    /**
     * Sets a supplier that creates a special bean mirror instead of the generic {@code BeanMirror} when requested.
     *
     * @param supplier
     *            the supplier used to create the bean mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     */
    BeanBuilder specializeMirror(Supplier<? extends BeanMirror> supplier);

    /**
     * Marks the bean as synthetic.
     *
     * @return this installer
     */
    BeanBuilder synthetic(); // Maybe on template?
}