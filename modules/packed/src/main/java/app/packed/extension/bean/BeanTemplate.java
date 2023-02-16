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

import java.util.EnumSet;
import java.util.List;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.context.ContextTemplate;
import app.packed.extension.operation.OperationTemplate;
import internal.app.packed.lifetime.PackedBeanTemplate;

/**
 * Ideen er man started med en af de foruddefinered templates, og saa laver man modification
 */
// Contexts, Args
// Lifetime
// Pouched-non-pouched
public sealed interface BeanTemplate permits PackedBeanTemplate {

    /**
     * Represents a bean whose lifetime is identical to that of its container.
     * <p>
     * A single instance of the bean will be created (if the instance was not already provided when installing the bean)
     * when the container is instantiated. Where after its lifecycle will follow that of the container.
     * <p>
     * Beans that are part of the container's lifecycle
     * <p>
     * A bean created using this template never has has any lifetime operations. As the lifetime of the bean is completely
     * controlled by the container in which is installed into.
     * <p>
     * If a container is {@link app.packed.lifetime.LifetimeKind#UNMANAGED} the new bean will be unmanaged.
     */
    BeanTemplate CONTAINER = new PackedBeanTemplate(BeanKind.CONTAINER);

    /**
     * The lifetime of the bean is not managed by any extension. At least not in a standard way
     * <p>
     * {@link #operations()} always returns a empty list
     * <p>
     * All operations on the bean must take a bean instance.
     * <p>
     * Giver det mening overhoved at supportere operation
     * <p>
     * It is a failure to use lifecycle annotations on the bean
     **/
    BeanTemplate EXTERNAL = new PackedBeanTemplate(BeanKind.MANYTON);

    /**
     * A single instance of the bean is created lazily when needed.
     * <p>
     *
     * @see BeanInstaller#install(Class)
     * @see BeanInstaller#installIfAbsent(Class, Consumer)
     * @see BeanInstaller#install(Op)
     * @see Map#isEmpty()
     */
    BeanTemplate LAZY = new PackedBeanTemplate(BeanKind.LAZY);

    BeanTemplate MANAGED = new PackedBeanTemplate(BeanKind.MANYTON);

    /**
     * A bean that no instances.
     * <p>
     * And hence no lifecycle then bean instance can go through.
     * <p>
     * The lifetime of the bean is identical to its container. A bean can never
     * <p>
     * Beans that use this template must either use a bean class source or no bean source. Attempting to use an Op or an
     * instance when installing the bean will fail with {@link app.packed.bean.BeanInstallationException}.
     *
     * @see BeanInstaller#install(Class)
     * @see BeanInstaller#installIfAbsent(Class, java.util.function.Consumer)
     * @see BeanInstaller#installWithoutSource()
     */
    BeanTemplate STATIC = new PackedBeanTemplate(BeanKind.STATIC);

    // An unmanaged bean will always return the bean instance.
    BeanTemplate UNMANAGED = new PackedBeanTemplate(BeanKind.MANYTON);

    // The bean is created by an operation
    // BeanHandle.attach
    // An instance is created for the lifetime of an operation
    BeanTemplate Z_FROM_OPERATION = new PackedBeanTemplate(BeanKind.MANYTON);

    default BeanTemplate inBeanContext(ContextTemplate context) {
        throw new UnsupportedOperationException();
    }

    default BeanTemplate inFactoryContext(ContextTemplate context) {
        throw new UnsupportedOperationException();
    }

    /**
     * The return type of 0
     * <p>
     * More technically this means the return type of the {@code operations().get(0).invocationType()} is the specified
     * class.
     *
     * @param clazz
     * @return a new template
     * @throws UnsupportedOperationException
     *             if this template is not based on {@link #MANAGED} or {@link #UNMANAGED}
     */
    default BeanTemplate instanceAs(Class<?> clazz) {
        return this;
    }

    /** {@return a list of the various lifetime operations for this bean template.} */
    default List<OperationTemplate> operations() {
        return List.of();
    }
}

interface Sandbox {

    /** {@return the allowwed bean source kinds for.} */
    default EnumSet<BeanSourceKind> sourceKinds() {
        return EnumSet.allOf(BeanSourceKind.class);
    }

    // Ahh alt er raw
    default BeanTemplate raw() {
        return null;
    }

    default boolean isBasedOn(BeanTemplate template) {
        // return template.isUnmodified() && this.base = template
        return false;
    }

    // Maa man goere paa installeren..
//    default <T> void initializeLocal(BeanLocal<T> local, T value) {
//
//    }

//    // No seperet MH for starting, part of init
//    // Tror maaske det her er en seperat template
//    Builder autoStart();

//
//    // Ideen er lidt at vi som default returnere Object vil jeg mene
//    // Men man kunne sige fx AbstractEntityBean at bean.init returnere
//    Builder instanceAs(Class<?> clazz);
//
//    default Builder inContext(ContextTemplate template) {
//        // This means Context args are added to all operations.
//
//        // builderManyton().inContext(WebContext.template).builder();
//        return this;
//    }
}
