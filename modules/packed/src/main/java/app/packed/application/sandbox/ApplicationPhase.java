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
package app.packed.application.sandbox;

/** The phase in the lifecycle of an application. */
// Another name than 
// Taenkt som naar man bygger ting og fx bruger IO.
// Saa er det maaske fint an indikere hvornaar man goer hvad.
public enum ApplicationPhase {
    BUILD_TIME, RUNTIME;
}
// Build_Time
//// Compose
//// Code generation
// Run_time
// 