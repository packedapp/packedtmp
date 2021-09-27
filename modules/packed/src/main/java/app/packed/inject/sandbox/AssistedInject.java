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
package app.packed.inject.sandbox;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */

// Metoder der selekter

// A trait generating method???

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AssistedInject {

    // only looks for @Inject??
    // Den traels at skulle putte hver member
    // Taenker maaske det er default og ikke noget man kan styre...
    // Problemet er lidt, forventer vi så også initialize???
    boolean injectMembers() default false;

    /* De næste 3 attributer styre praecis hvilken metode/constructor vi rammer. */
    Class<?> actualType() default Object.class;

    // If defined looks for a static method
    /**
     * If this attribute is different from the empty string (which is the default value). The runtime will look for a static
     * (factory) method instead.
     * 
     * @return a method name of a static method. Or empty
     */
    String methodName() default "";

    // If Multiple constructors
    // Good idea to fill out if you create shit from API's you do not control
    Class<?>[] parameterTypes() default {};// if it takes no parameter use default method

    // The index of to where each parameter must be mapped
    // if non-default, must have an int for every parameter

    // Default resolution mode (no parameters specified)
    /// If there are more than one of each type. Either the same of
    // types must be specified in the signature. Or use parameters

    int[] parameters() default {}; // if it takes parameters of the same type
    
    // Class<? super InvocationBinder> nahh ved sgu ikke om det er mere simpelt...
}
// How to handle generic types??? Jeg taenker
// primaert i forbindelse med matching