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
package app.packed.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Incid
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented // ???Hmm
@interface StaticConstruction {}

// What about opens of the target?????
// On the same class it is fine

//staticInitializerOf(Class<?> clazzz) //Looks for methods named of using the same algorithm as @Inject
//Maybe only public???

//Use a static method to construct an instance of this class
//@StaticConstruction(Class<?> target, String methodName, Class<?>[] parameterTypes)

//Skal ogsaa have static metoder... Factory.staticOf() 
