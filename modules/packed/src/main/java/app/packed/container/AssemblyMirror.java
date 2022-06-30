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

import app.packed.application.ApplicationMirror;
import packed.internal.container.Mirror;
import packed.internal.container.UserRealmSetup;

/** A mirror of an {@link Assembly}. */
public sealed interface AssemblyMirror extends Mirror permits UserRealmSetup.BuildtimeAssemblyMirror {

    /** {@return the application this assembly is a part of.} */
    ApplicationMirror application();
    
    /** {@return the class that defines the assembly.} */
    Class<? extends Assembly> assemblyClass();

    /** {@return a stream of all assemblies that have been linked from this assembly.} */
    Stream<AssemblyMirror> children();

    /** {@return the root container defined by this assembly.} */
    ContainerMirror container();

    /** {@return a list of hooks that are applied to containers defined by the assembly.} */
    List<Class<? extends ContainerHook>> containerHooks();

    /** @return whether or not this assembly defines the root container in the application.} */
    boolean isRoot();

    /**
     * {@return any assembly that linked this assembly, or empty if the assembly defined the root container of an
     * application.}
     */
    Optional<AssemblyMirror> parent();
}
