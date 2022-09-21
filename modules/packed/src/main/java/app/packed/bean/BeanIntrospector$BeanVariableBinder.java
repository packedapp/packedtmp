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

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanIntrospector.BeanElement;
import app.packed.operation.Op;
import app.packed.operation.Variable;
import app.packed.operation.bindings.BindingMirror;

/**
 *
 */

// Eller ogsaa peeler vi inde vi kalder provide

// Med alle de andre bean ting. Saa har vi en BeanField->Operation
// Skal vi have noget lige saadan her BeanDependency->Provisioning
// eller BeanVariable -> Dependency???
// Saa kan vi strippe af paa BeanVariable
// Saa bliver BeanVariable

public non-sealed interface BeanIntrospector$BeanVariableBinder extends BeanElement {

    /**
     * <p>
     * Vi tager Nullable med saa vi bruge raw.
     * <p>
     * Tror vi smider et eller andet hvis vi er normal og man angiver null. Kan kun bruges for raw
     * 
     * @param instance
     *            the instance to provide to the variable
     * 
     * @throws ClassCastException
     *             if the type of the instance does not match the type of the variable
     * @throws IllegalStateException
     *             if a provide method has already been called on this instance (I think it is fine to allow it to be
     *             overriden by itself).
     */
    void bind(@Nullable Object obj);

    /**
     * Variable is resolvable at runtime.
     * <p>
     * Cannot provide instance. Must provide an optional class or Null will represent a missing value. Maybe just optional
     * class for now
     * 
     * @return
     */
    // Hmm, resolve at runtime ved jeg ikke hvor meget passer. extensionen ligger jo fast
    // Saa maaske bindAtRuntime
    BeanIntrospector$BeanVariableBinder bindAtRuntime();

    /**
     * <p>
     * For raw er det automatisk en fejl
     */
    // provideUnresolved();
    void bindMissing(); // Giver ikke mening for rawModel

    void provide(MethodHandle methodHandle);

    void provide(Op<?> fac);

    /**
     * @return
     * 
     * @throws BeanDefinitionException
     *             if the variable was a proper key
     */
    default Key<?> readKey() {
        throw new UnsupportedOperationException();
    }

    BeanIntrospector$BeanVariableBinder specializeMirror(Supplier<? extends BindingMirror> supplier);

    TypeInfo type();
    
    Variable variable();

    interface TypeInfo {

        void checkAssignable(Class<?> clazz, Class<?>... additionalClazzes);

        boolean isAssignable(Class<?> clazz, Class<?>... additionalClazzes);

        Class<?> rawType();
    }
}
