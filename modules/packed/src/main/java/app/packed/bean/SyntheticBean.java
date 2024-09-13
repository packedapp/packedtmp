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

import java.util.function.Consumer;

import app.packed.operation.Op;

/**
 *
 */
// Skal vi have et lookup???
public interface SyntheticBean<T> {

    SyntheticBean<T> reconfigure(Consumer<? super BeanClassMutator> action);

    // A bean can be synthetic but with a source
    BeanSourceKind source();

    // Altsaa kan vi bare aendre den??
    static <T> SyntheticBean<T> of(Class<T> beanClass, Consumer<? super BeanClassMutator> action) {
        throw new UnsupportedOperationException();
    }

    // What is the beanClass()? SyntheticBean
    static <T> SyntheticBean<T> of(Consumer<? super BeanClassMutator> action) {
        throw new UnsupportedOperationException();
    }

    // Like instance it is fairly limited what you can do
    static <T> SyntheticBean<T> of(Op<?> op, Consumer<? super BeanClassMutator> action) {
        throw new UnsupportedOperationException();
    }

    // I think it is more of a builder you return
    static <T> SyntheticBean<T> ofInstance(T instance, Consumer<? super BeanClassMutator> action) {
        throw new UnsupportedOperationException();
    }
}

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