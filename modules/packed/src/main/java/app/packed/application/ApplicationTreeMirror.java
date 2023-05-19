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

import app.packed.container.AssemblyTreeMirror;
import internal.app.packed.container.Mirror;
import internal.app.packed.container.TreeMirror;

/**
 *
 */
// Problemet med ApplicationTree er vel navngivning
public interface ApplicationTreeMirror extends TreeMirror<ApplicationMirror>, Mirror {

    AssemblyTreeMirror assemblies();
}

//ApplicationMirror bootstappedBy(); // nah hvad hvis det er et child som root.
// Maa have noget paa Application som Host? Ikke parent da det ikke er en Application
