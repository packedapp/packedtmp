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
package app.packed.bundle.x;

import app.packed.bundle.WiringOperation;

/**
 *
 */
public abstract class ContainerWiringOperations {
    public static final ContainerImportStage NO_STARTING_POINTS = null;
    public static final ContainerImportStage NO_STOPPING_POINTS = null;

    // Ideen er at kunne navngive noden man importere som
    // Maaske endda have den some en ekstra metode
    // installContainer(JettyServer.class, nameRoot("foobar)
    // or rootName, rootNamePeek?
    public static WiringOperation nameRoot(String newName) {
        throw new UnsupportedOperationException();
    }

    // I guess this also means you cannot import services from it....
    public static WiringOperation allowUnwiring() {
        throw new UnsupportedOperationException();
    }

    // Something with description

    // We provide 3 types of starter bundle
    // Bundle ---------| No build in functionality
    // InjectorBundle -| Services + Listeners
    // ContainerBundle | Services + Listeners + Lifecycle + Component Hooks

    // ComponentInstanceHook (type), ComponentMethodHook (annotation+Type?), ComponentFieldHook (annotation+Type?)

    // Hooks burde jo ogsaa vaere AOP....
    // Og hvad med AOP paa injectorer.... Ja det er vel der den bliver foedt...
    //

}
