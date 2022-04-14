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

import app.packed.base.Key;
import app.packed.bean.hooks.sandbox.InvokerConfiguration;
import app.packed.bean.operation.Operation;
import app.packed.bean.operation.driver.OperationDriver;
import app.packed.bean.operation.driver.OperationDriver2;
import app.packed.bean.operation.mirror.OperationMirror;
import app.packed.inject.Factory;
import packed.internal.bean.PackedBeanHandle;

/**
 * A bean driver must be created via {@link BeanSupport}.
 */
// INFO (type, kind)
// Operations (add synthetic/functional)
// Sidecars (add)
// Lifecycle (Custom Factory, InvocationConfigurations)
// Services (bind, bindContext)

// NamePrefixs
// Scan (disable, do scan) ???
// callacbks, onBound, onBuild, ...

// Add Builder???
@SuppressWarnings("rawtypes")
public sealed interface BeanHandle<T> permits PackedBeanHandle {

    // Make into build method???
    BeanHandle<T> commit();

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

    Operation addFunctionOperation(Object functionInstance);

    default Operation addOperation(@SuppressWarnings("exports") OperationDriver driver) {
        throw new UnsupportedOperationException();
    }

    default void addOperation(@SuppressWarnings("exports") OperationDriver2 driver) {
        throw new UnsupportedOperationException();
    }

    interface Builder<T> {

        BeanHandle<T> build();
    }
}

interface BeanDriverSandbox<T> {

    default void synthetic() {
        // or hidden();
    }

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

    // Provide stuff, state holder, Lifecycle

    //////////////// Sidecars
    default void sidecarAddInstance(Object o) {}

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
