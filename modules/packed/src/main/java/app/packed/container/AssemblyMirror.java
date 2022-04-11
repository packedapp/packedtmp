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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.component.ComponentMirror;
import app.packed.component.Realm;
import packed.internal.container.AssemblySetup;

/**
 *
 */
public sealed interface AssemblyMirror permits AssemblySetup.BuildtimeAssemblyMirror {

    /** {@return a stream of all assemblies that have been linked from this assembly.} */
    Stream<AssemblyMirror> children();

    /** {@return a stream of all components defined by the assembly.} */
    Stream<ComponentMirror> components();

    List<Class<? extends ContainerHook.Processor>> containerHooks();

    Realm owner();

    Optional<AssemblyMirror> parent();

    /** {@return the root container defined by this assembly.} */
    ContainerMirror root();

    Class<? extends Assembly> type();

}
