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
package app.packed.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE })
/**
 *
 */
// Hvis den er paa typen, kan man bruge den uden de andre annoteringer..

// As BeanParam see https://eclipse-ee4j.github.io/jersey.github.io/documentation/latest/jaxrs-resources.html#d0e2271
// (3.15)

// Aggregate?

// @Inject paa en parameter....? Maybe...

// Ej, tror den er ret sjov at implementere...

// Foerst taenkte jeg vi kunne bruge @Inject.
// Men den er jo allerede brugt til at injecte almindelige services paa et field......

// Man behover ikke combinere med @Inject paa fields

// @Create MyStuff

// Maaske var det i virkeligheden feder hvis det var et interface????
// End en annotering, saa

public @interface Populate {}

// Hmm spaendende.... Den her kan man jo teoretisk, l
// Eneste er at vi ikke har support for de der specielle ting
// Tror bedre jeg kan lide annoteringen...
interface Aggregate {}

//// Spec

// Enten en constructor, eller en statisk @Inject metode

// Kan vaere optional @Populate i hvilket tilfaelde
// * Eller dependencies i den automatisk er optional
// * Er kun non-empty hvis alle non-optional dependencies er fullfilled

// Og ellers fungere alt omkring injection paa den. Ogsaa f.eks.
// AnnotatedVariableParameter.

// I virkeligheden er der vel tale om en prototype JIT service (Guice JIT Binding)
// Hvis man f.eks. ber om en logger i den....
/// Saa er det jo en anden klasse en selve komponent klassen....
