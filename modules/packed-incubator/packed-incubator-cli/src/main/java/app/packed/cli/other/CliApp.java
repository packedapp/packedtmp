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
package app.packed.cli.other;

import app.packed.application.App;
import app.packed.application.ApplicationInterface;
import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import app.packed.container.Wirelets;

/**
 *
 */
// Maybe it is just a thin wrapper for App

// Maybe an assembly instead of an interface????
// CliAssembly / MainAssembly

@Deprecated
public interface CliApp extends ApplicationInterface {

    // But isn't this normally in the code???
    // Maybe a StopOption?
    // ObjectToInt(ApplicationException->int errorCode)

    static void run(Assembly assembly, Wirelet... wirelets) {
        // Maybe checked run...
        App.run(assembly, wirelets);
    }

    static void run(Assembly assembly, String[] args, Wirelet... wirelets) {
        // Maybe checked run...
       // App.launcherOf(assembly, wirelets).args(args).
        App.run(assembly, Wirelets.argList(args).andThen(wirelets));
    }
}
