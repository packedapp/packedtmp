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

/**
 *
 */
public class BeanFactoryMirror {
    // Den her kan jo evt. have mere info om factory...
}

//Should this extend Lifecycle operation????
//Maaske er det ikke en operation??? 
//Giver ikke mening at have error handle, eller lifecycle
//Det er ikke en operation... IDK

// Det der taler lidt imod at det er en operation er fx
// ServiceProvide(Prototype) jo ogsaa er en operation...
// Saa invokere vi lige pludselig 2 operationer...
// Og hvad med ErrorHandlingen den ligger vel hos
// ServiceProvide, lad os fx sige vi har