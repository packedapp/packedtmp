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
import internal.app.packed.container.Mirror;
import internal.app.packed.container.UserRealmSetup;

/** A mirror of an {@link Assembly}. */
public sealed interface AssemblyMirror extends Mirror permits UserRealmSetup.BuildtimeAssemblyMirror {

    /** {@return the application this assembly is a part of.} */
    ApplicationMirror application();

    /** {@return the class that defines the assembly.} */
    Class<? extends Assembly> assemblyClass();

    /**
     * {@return a stream of any child assemblies defined by this assembly.}
     * 
     * @see ContainerConfiguration#link(Assembly, Wirelet...)
     */
    Stream<AssemblyMirror> children();

    /** {@return the root container defined by this assembly.} */
    ContainerMirror container();

    /** {@return a list of hooks that are applied to containers defined by the assembly.} */
    List<Class<? extends ContainerHook>> containerHooks();

    /** @return whether or not this assembly defines the root container in the application.} */
    boolean isRoot();

    /**
     * {@return the parent of this assembly, or empty if the assembly defines the root container of the application.}
     */
    Optional<AssemblyMirror> parent();
}


///**
//* {@return the module of the application. This is always the module of the Assembly or ComposerAction class that
//* defines the application container.}
//* 
//* Altsaa hvis en application skal have et module... Skal container+Bean vel ogsaa
//*/
////Hmm, hvis applikation = Container specialization... Ved component
////Tror maaske ikke vi vil have den her, IDK... HVad med bean? er det realm eller bean module
////Maaske vi skal have et realm mirror????
//Module module();
