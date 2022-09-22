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

import app.packed.bean.BeanIntrospector.BeanElement;

/**
 *
 * <p>
 * Members from the {@code java.lang.Object} class are never returned.
 */

// Kig maaske i Maurizio Mirror thingy...
public non-sealed interface BeanIntrospector$OnClassHook extends BeanElement {

    boolean hasFullAccess();
    
    void forEachConstructor(Consumer<? super BeanIntrospector$OnConstructorHook> m);
    
    void forEachMethod(Consumer<? super BeanIntrospector$OnMethodHook> m);

    // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
    // Masske har vi BeanClass.Builder() istedet for???

    // Cute men vi gider ikke supportere det
//    static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
//        throw new UnsupportedOperationException();
//    }

    // Fields first, include subclasses, ... blabla
    // Maybe on top of full access have boolean custom processing on ClassHook
    void setProcessingStrategy(Object strategy);
}