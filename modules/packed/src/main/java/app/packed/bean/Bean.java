/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.util.Set;

import app.packed.operation.Op;
import app.packed.util.AnnotationList;
import internal.app.packed.bean.PackedBean;

/**
 * Represents an immutable bean that can be installed.
 */

// Info
//// BeanClass  (Write Only?)
//// BeanSourceKind  (Write Only)
//// Name
//// ComponentTags
//// Lookup (Write Only)
//// Key (WithDefaultKey) (Optional??)

//BeanClassInfo
//// Annotations
//// Methods
//// Fields
//// Constructors

// add -> (For list only)(
// transform -> Takes consumer
// withXXX -> Returns new with replacement

// active
//// findExtensions-> Find all extensions
//// addForeignBeanTrigger... (Used mainly for testing I think)

/// foreignBeanTriggers er jo lidt problemet med eager scan...

// I think classes in the java. namespace are always ignored

// Skal vi have et withLookup???
// Vi kunne ogsaa godt supportere lazy scan her, Saa kan man faa debug info, og det kan genbruges paa runtime

// I don't know if we expose Bean bean()??? I don't think so. If we allow registration of Lookup objects
// Problem is for example, component tags. If you override it via BeanConfiguration.componentTags it will not match

public sealed interface Bean<T> permits PackedBean {

    /** {@return a list of annotations on the bean} */
    AnnotationList annotations();

    /** {@return the source kind the bean was created from} */
    BeanSourceKind beanSourceKind();

    Class<?> beanClass();

    default Set<String> componentTags() {
        return Set.of();
    }

    // I think we do it now...
    // withTransformation???

    // Hvordan interakter vi med Annoteringer her?
    default Bean<T> withComponentTags(Set<String> tags) {
        return this;
    }

    /**
     * Creates a new sourceless bean.
     *
     * @return the new bean
     */
    static Bean<?> of() {
        return PackedBean.of();
    }

    static <T> Bean<T> of(Class<T> beanClass) {
        return PackedBean.of(beanClass);
    }

    // Like instance it is fairly limited what you can do

    // This should probably only return the Op as single construtor
    static <T> Bean<T> of(Op<?> op) {
        return PackedBean.of(op);
    }

    // I think it is more of a builder you return
    /**
     * Creates a new bean based on the specified bean instance.
     *
     * @param <T>
     *            the type of the bean instance
     * @param instance
     *            the bean instance
     * @return the new bean
     */
    // I think we return 0 constructors
    static <T> Bean<T> ofInstance(T instance) {
        return PackedBean.ofInstance(instance);
    }

    static <T> Builder<T> builderOfInstance(T instance) {
        throw new UnsupportedOperationException();
    }

    interface Builder<T> {
        Bean<T> build();
    }
}

// Or simply Builder....

//Alternativ hedder den noget andet ala BeanFactory (or just Bean).. Og vi laver den fra alle metoder install(Class)->BeanFactory.of(Class)->install(BeanFactory)

//Was SyntheticBean

//Can be made from scratch, using an optional source and specified by the developer
//Can mutate a bean before it is introspected for example via a bean build hook

//There

//BeanBuildTrigger.installAll(ASS, b->b.debugInstantiantion());

//Virtual vs Synthetic. I think ideally we want something that is not in the JDK
//Fx synthetic beanMethod.modifiers() may return synthetic or a non-synthetic method

//SyntheticBean?? beanMirror.isVirtual sounds not right

//Okay, I only think authorities can manipulate their own beans

//Whenever you do manipulation of a non-synthetic bean. You get a syntheticBean (Do we store linage??)

//En ny source type
//Man kan lave instancer

//Ideen er lidt du kan goere hvad du vil.

//Og til sidst lade frameworket generere en implementation

//En af usecasene

//Adapted/Customized

//of() (SyntheticBean) vs functional bean??? Hmm
//of() and then add functions...
//function -> Lambda
//Operation -> Op?