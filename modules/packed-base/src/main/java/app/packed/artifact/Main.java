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
package app.packed.artifact;

import app.packed.component.Bundle;
import app.packed.component.Wirelet;
import app.packed.lifecycleold.RunState;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.lifecycle.PackedAssemblyContext;
import packed.internal.lifecycle.PackedInitializationContext;

/**
 *
 */
public class Main {

    /**
     * This method will create and start an {@link App application} from the specified source. Blocking until the run state
     * of the application is {@link RunState#TERMINATED}.
     * <p>
     * Entry point or run to termination
     * 
     * @param source
     *            the source of the application
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application did not execute properly
     */
    // add exitOnEnter() <--- so useful for tests
    // exitable daemon...
    // https://github.com/patriknw/akka-typed-blog/blob/master/src/main/java/blog/typed/javadsl/ImmutableRoundRobinApp.java3
    static void execute(Bundle<?> source, Wirelet... wirelets) {
        execute0(source, wirelets);
    }

    // sync deamon???????
    // App.main(new Goo(), args);
    static void main(Bundle<?> bundle, String[] args, Wirelet... wirelets) {
        execute0(bundle, wirelets);
    }

    // Maybe more a.la. Main.execute()
    static void execute0(Bundle<?> source, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedAssemblyContext.assemble(source, 0, null, wirelets);
        ShellContext context = PackedInitializationContext.newShellContext(node, node.wirelets);
        context.start();
    }
}