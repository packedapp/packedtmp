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
package app.packed.build;

import java.util.Optional;

import app.packed.assembly.Assembly;
import internal.app.packed.build.PackedBuildProcess;

/**
 * Only available while building the application.
 */
//Ideen er lidt at man kan faa access til Build info.

//Og fx hvis vi har hot reload tilfoeje en klasse to watch udover selve assemblies

//Er den defineret i en ThreadLocal??
//Tjah hvorfor ikke... Men tjah hvorfor

//En overordnet "process" som maaske kan koere over flere omgange
//Saa har vi en BuildTask som jo bare et traa....

//Altsaa vi har jo altid en rod taenker jeg. Men saa kan vi have tasks som er delayed??
//Og codegen er jo noget andet... Er ikke sikker på vi registrere de tasks som mirrors
//Har vi en forrest???
public sealed interface BuildProcess permits PackedBuildProcess {

    /**
     * @return
     */
    Optional<Class<? extends Assembly>> currentAssembly();

    BuildGoal goal();

    /** { @return the unique build process id} */
    long processId();

    // Optional????
    static BuildProcess current() {
        return PackedBuildProcess.get();
    }
}
