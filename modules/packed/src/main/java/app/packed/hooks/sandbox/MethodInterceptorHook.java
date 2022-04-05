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
package app.packed.hooks.sandbox;

/**
 *
 */
// tror den er 100% separat fra MethodHook...
// Eller maaske er den ikke..:)

// Det store issue er jo ordering...
// https://docs.micronaut.io/2.4.0/guide/index.html#aop

@interface MethodInterceptorHook {

}

// Hmm skal vi bare altid specificere den...
// Hvis man bruger andre annotationer Saa bliver der kun lavet en
// bootstrap instance??? Nej det goer der alligevel altid...
// Hmm IDK virker lidt underligt

// Maaske har vi hellere noget priority??? idk
//boolean allowDecorate() default false;
