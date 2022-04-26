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
package app.packed.container.sandbox;

/**
 *
 */
// Strengt taget skulle bruge ikke kunne bruge denne ogsaa?

// Container Instance x Extension

// RuntimeExtensionContext? Just to make it clear
// Maybe even ContainerContext or BeanContext
public interface ExtensionBeanContext {}

//Naar man skal lave operation... Skal man have en Bean Tilknyttet
//Hvad hvis man

//Vi laver saadan en per Container Instance X Realm eller som Packed nu bestemmer

//Was BeanOperationInvocationContext
//May be ExtensionContainerContext... if users cannot create method handles
//Vi checker per build tid at den har en extension ejer...
//Man kan ikke injecte den i interceptors eller jo men saa er det jo interceptorens
//ejer

//Se ogsaa ExtensionContext. Som det bliver hvis brugere ikke skal kunne invokere disse ting

/* sealed */ interface ContainerRealmContext {

}
