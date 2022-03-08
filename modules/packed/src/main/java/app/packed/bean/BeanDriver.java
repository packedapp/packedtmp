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
import app.packed.bean.mirror.BeanOperationMirror;
import app.packed.inject.Factory;
import packed.internal.bean.PackedBeanDriver;

/**
 * A bean driver must be created via {@link BeanSupport}.
 */
//Alternativ name: BeanDefiner, Soeg videre under GraalmVM fra og med D
@SuppressWarnings("rawtypes")
public sealed interface BeanDriver<T> permits PackedBeanDriver {

    default FunctionalBeanOperationConfiguration addOperation() {
        throw new UnsupportedOperationException();
    }
    
    // Taenker den foerst bliver commitet naar man laver en configuration???

    default FunctionalBeanOperationConfiguration addOperationFunctional(Class<?> functionType, Object function) {
        throw new UnsupportedOperationException();
    }

    default void addSidecar(Class<?> clazz) {}

    default void addSidecar(Factory<?> clazz) {}

    //////////////// Sidecars
    default void addSidecarInstance(Object o) {}

    Class<?> beanClass();

    BeanKind kind();
    
    default void bindOperationMirror() {
        // bind(EntityMirror.class);
        // Mulighederne er uendelige, og
    }

    default <E> void bindService(Key<E> key, Class<E> implementation) {
        // bindService(WebRequestContext.class, WebRequestBeanContextImpl.class)
        // ServiceScope.Bean
    }

    default void checkWiring() {}

    default InvokerConfiguration factory() {
        throw new UnsupportedOperationException();
    }

    // Provide stuff, state holder, Lifecycle

    default InvokerConfiguration factory(Member member) {
        // Ideen er lidt at Member er en constructor
        // statisks field
        // static metode
        // Som skal returne en exact bean class
        
        // Men hvorfor ikke bare tage et Factory????
        throw new UnsupportedOperationException();
    }

    void prototype();

    interface FunctionalBeanOperationConfiguration {
        FunctionalBeanOperationConfiguration addMirror(Class<? extends BeanOperationMirror> bomType);

        FunctionalBeanOperationConfiguration name(String name);

        FunctionalBeanOperationConfiguration prefix(String prefix); // Maaske tilfoejer vi bare automatisk et prefix, hvis der eksistere en med samme navn
        /// IDK
    }
}
/// set properties
/// Bind operation (Eller er det hooks???)
/// make method handles, or runtime factories (maybe after build)
/// 

// Inject BeanManager<T>
// MH(ExtensionContext, )

/**
*
*/
class BeanZBuilder {

    public BeanConfiguration build() {
        return null;
    }

    public <C extends BeanConfiguration> C build(C configuration) {
        return configuration;
    }
}

/* sealed */ interface ZBuilder {

    ZBuilder build();

    // Specific super type

    // Den kan vi jo se fra Typen af configuration...
    ZBuilder kind(BeanKind kind);

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
