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
package app.packed.container;

import app.packed.application.ApplicationLocalAccessor;
import sandbox.extension.container.ContainerHandle;

/** An accessor where {@link ContainerLocal container local} values can be stored and retrieved. */
public sealed interface ContainerLocalAccessor extends ApplicationLocalAccessor permits ContainerConfiguration, ContainerHandle, ContainerMirror {}
//Extension?
//En god maade at traekke sig selv ud...
//ContainerLocal<FooExtension> myLocal = FooExtension.local();
