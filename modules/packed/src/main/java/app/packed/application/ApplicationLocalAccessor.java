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

import app.packed.assembly.Assembly;
import app.packed.container.ContainerLocalAccessor;

/** An entity where {@link ApplicationLocal application local} values can be manipulated. */

// I really want it as an inner interface...

// Cannot use it before it is applied to a container tree...
// What about after use??? I think may be os. I think it is needed for mirrors...

//The only thing about Assembly??? is that this cannot be called always. Build needs to be started (Needs to be part of BuildProcess, otherwise values potentially needs to merged)
//
public sealed interface ApplicationLocalAccessor permits ContainerLocalAccessor, ApplicationConfiguration, ApplicationMirror, Assembly {}
