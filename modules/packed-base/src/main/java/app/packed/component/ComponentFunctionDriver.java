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
package app.packed.component;

/**
 *
 */

// Hvad kan den anden end at registrere en function???
// Ser det ihvertfald ikke som en sidecar...
// Og heller ikke som noget hvor man skulle kunne angive.
// En klasse/

// Er det en source????
// Nej vil jeg ikke mene:)
// Packed holder ikke nogen reference til den...
// Vi scanner den ikke
// Vi instantiere den ikke
public interface ComponentFunctionDriver<C, F> {

    ComponentDriver<C> bindFunction(F function);
}
