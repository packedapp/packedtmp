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

import java.lang.reflect.Member;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.bean.hooks.sandbox.InvokerConfiguration;
import app.packed.bean.operation.OperationHandle;
import app.packed.bean.operation.OperationMirror;
import app.packed.bean.operation.driver.OperationDriver;
import app.packed.bean.operation.driver.OperationDriver2;
import app.packed.extension.ExtensionPointContext;
import app.packed.inject.Factory;
import packed.internal.bean.PackedBeanHandle;
import packed.internal.bean.PackedBeanHandleBuilder;

/**
 * A bean driver must be created via {@link BeanExtensionPoint}.
 */
// INFO (type, kind)

// Operations (add synthetic/functional)

// Sidecars (add)

// Lifecycle (Custom Factory, InvocationConfigurations)

// Services (bind, bindContext)

// callacbks, onBound, onBuild, ...
@SuppressWarnings("rawtypes")
public sealed interface BeanHandle<T> permits PackedBeanHandle {

    OperationHandle addFunctionOperation(Object functionInstance);

    default OperationHandle addOperation(@SuppressWarnings("exports") OperationDriver driver) {
        throw new UnsupportedOperationException();
    }

    default void addOperation(@SuppressWarnings("exports") OperationDriver2 driver) {
        throw new UnsupportedOperationException();
    }

    default void addMirror(Supplier<? extends BeanMirror> mirrorFactory) {}

    /**
     * Registers a wiring action to run when the bean becomes fully wired.
     * 
     * @param action
     *            a {@code Runnable} to invoke when the bean is wired
     */
    BeanHandle<T> addWiringAction(Runnable action);

    /**
     * @return
     * 
     * @see BeanConfiguration#beanClass()
     * @see BeanMirror#beanClass()
     */
    Class<?> beanClass(); // beanSource instead??

    /**
     * @return
     * 
     * @see BeanMirror#beanKind()
     * @see BeanConfiguration#beanKind()
     */
    BeanKind beanKind();

    /**
     * A builder for a bean handle. Can only be created via {@link BeanExtensionPoint}.
     * 
     * @see BeanExtensionPoint#newBuilder(BeanKind)
     * @see BeanExtensionPoint#newBuilderFromClass(BeanKind, Class)
     * @see BeanExtensionPoint#newBuilderFromFactory(BeanKind, Factory)
     * @see BeanExtensionPoint#newBuilderFromInstance(BeanKind, Object)
     */
    sealed interface Builder<T> permits PackedBeanHandleBuilder {
        // Scan (disable, do scan) ???

        /**
         * @return
         * 
         * @see BeanConfiguration#beanClass()
         * @see BeanMirror#beanClass()
         */
        Class<?> beanClass(); // vs BeanClass??? // beanSource instead??

        /**
         * @return
         * 
         * @see BeanMirror#beanKind()
         * @see BeanConfiguration#beanKind()
         */
        BeanKind beanKind();

        BeanHandle<T> build();

        /**
         * Marks the bean as owned by the extension representing by specified extension point context
         * 
         * @param context
         *            a context representing the owner of the bean
         * @return this builder
         */
        Builder<T> forExtension(ExtensionPointContext context);

        /**
         * Sets a prefix that is used for naming the bean.
         * <p>
         * If there are no other beans with the same name (for same parent container) when creating the bean. Packed will use
         * the specified prefix as the name of the bean. Otherwise, it will append a postfix to specified prefix in such a way
         * that the name of the bean is unique.
         * 
         * @param prefix
         *            the prefix used for naming the bean
         * @return this builder
         */
        default Builder<T> namePrefix(String prefix) {
            return this;
        }
    }
}

interface BeanXDriverSandbox<T> {

    default <E> void bindService(Key<E> key, Class<E> implementation) {
        // bindService(WebRequestContext.class, WebRequestBeanContextImpl.class)
        // ServiceScope.Bean
    }

    // Ved ikke rigtig usecasen. Fordi den skal ikke bruges fra BeanConfiguration
    // Der er allerede en vi kan bruge.
    default void checkWiring() {}

    default InvokerConfiguration factory() {
        throw new UnsupportedOperationException();
    }

    default InvokerConfiguration factory(Member member) {
        // Ideen er lidt at Member er en constructor
        // statisks field
        // static metode
        // Som skal returne en exact bean class

        // Men hvorfor ikke bare tage et Factory????
        throw new UnsupportedOperationException();
    }

    default FunctionalBeanOperationConfiguration operationAdd() {
        throw new UnsupportedOperationException();
    }

    default FunctionalBeanOperationConfiguration operationAddFunctional(Class<?> functionType, Object function) {
        throw new UnsupportedOperationException();
    }

    default void sidecarAdd(Class<?> clazz) {}

    default void sidecarAdd(Factory<?> clazz) {}

    //////////////// Sidecars
    default void sidecarAddInstance(Object o) {}

    // Provide stuff, state holder, Lifecycle

    default void synthetic() {
        // or hidden();
    }

    @SuppressWarnings("exports")
    interface FunctionalBeanOperationConfiguration {
        FunctionalBeanOperationConfiguration addMirror(Class<? extends OperationMirror> bomType);

        FunctionalBeanOperationConfiguration name(String name);

        FunctionalBeanOperationConfiguration prefix(String prefix); // Maaske tilfoejer vi bare automatisk et prefix, hvis der eksistere en med samme navn
        /// IDK
    }
}
// Alternativ name: BeanDefiner, Soeg videre under GraalmVM fra og med D

/// set properties
/// Bind operation (Eller er det hooks???)
/// make method handles, or runtime factories (maybe after build)
/// 

// Inject BeanManager<T>
// MH(ExtensionContext, )

/* sealed */ interface ZBuilder {

    default void bindOperationMirror() {
        // bind(EntityMirror.class);
        // Mulighederne er uendelige, og
    }

    ZBuilder build();

    // Specific super type

    ZBuilder namePrefix(Function<Class<?>, String> computeIt);

    ZBuilder namePrefix(String prefix);

    ZBuilder noInstances();

    // BeanConfigurationBinder<BeanConfiguration> buildBinder();
    ZBuilder noReflection();

    ZBuilder oneInstance();

    // Vi kan ikke rejecte extensions paa bean niveau...
    //// Man kan altid lave en anden extension som bruger den extension jo
    //// Saa det er kun paa container niveau vi kan forbyde extensions

    //// For instantiationOnly
    // reflectOnConstructorOnly();

    // reflectOn(Fields|Methods|Constructors)
    // look in declaring class
}

//BeanBuilder, BeanRegistrant

//BeanInstanceMaker??? Nope, det er ikke kun bean instances...
//Men vi har maaske n ekstra
