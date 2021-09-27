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

/**
 * 
 * Unless otherwise specified, constant is the default mode.
 */
// Jeg er faktisk ikke vild med den...
public enum ServiceMode {
    CONSTANT, TRANSIENT;
}
// Prototype, scopeless, ephemeral, 

// Permanent
//Teknisk set behoever vi ikke denne.
//Og i lang tid klarede vi os med isConstant()
//Men den er rigtig god for forstaaelsen og kunne linke til ting.
//I hvad der uden tvivl bliver rigtigt svaert at forklare.
// forskellen paa component og service scope
