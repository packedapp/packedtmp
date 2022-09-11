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
import app.packed.inject.Factory;
import app.packed.operation.dependency.DependencyMirror;

/**
 *
 */

// Eller ogsaa peeler vi inde vi kalder provide

// Med alle de andre bean ting. Saa har vi en BeanField->Operation
// Skal vi have noget lige saadan her BeanDependency->Provisioning
// eller BeanVariable -> Dependency???
// Saa kan vi strippe af paa BeanVariable
// Saa bliver BeanVariable

public non-sealed interface BeanDependency extends BeanElement {

    void provide(Factory<?> fac);

    void provide(MethodHandle methodHandle);

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
    void provideInstance(@Nullable Object obj);

    /**
     * <p>
     * For raw er det automatisk en fejl
     */
    // provideUnresolved();
    void provideMissing();

    /**
     * @return
     * 
     * @throws BeanDefinitionException
     *             if the variable was a proper key
     */
    default Key<?> readKey() {
        throw new UnsupportedOperationException();
    }

    /**
     * Variable is resolvable at runtime.
     * <p>
     * Cannot provide instance. Must provide an optional class or Null will represent a missing value. Maybe just optional
     * class for now
     * 
     * @return
     */
    BeanDependency runtimeOptional(); // optionalAtRuntime

    BeanDependency specializeMirror(Supplier<? extends DependencyMirror> supplier);

    TypeInfo type();

    interface TypeInfo {

        void checkAssignable(Class<?> clazz, Class<?>... additionalClazzes);

        boolean isAssignable(Class<?> clazz, Class<?>... additionalClazzes);

        Class<?> rawType();
    }
}
