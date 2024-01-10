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
package app.packed.application;

/**
 *
 */
// Det der blev kaldt ConfigSite engang

// Problemet med fx BuildSite som jeg ser det.
// Er at nogen helt sikkert for wirelets, eller fx noget Config
// vil spoerge efter hvilke wirelets der er specificeret paa runtime...

// Altsaa det er vel mere eller mindre end StackTraceElement

// Vil mene


// Application, Assembly, Container, Bean, Operation (BuildSite, AnnotatedField, AnnotatedMethod)

// ? Namespace (Is mostly implicitly created). So maybe just the first place it is referenced, for example, first provide
// Not so much. Binding
public interface BuildSite {

}
// Generalt har vi 3 configurations muligheder

// build a.la. call stuff
// Wirelets
// Config [ Files, Manual]

// ConfigTracer or just @Debug <-- Not sure it is devops... Think we would want to use it in production

