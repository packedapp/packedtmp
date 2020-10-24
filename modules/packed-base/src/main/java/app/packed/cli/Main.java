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
package app.packed.cli;

import app.packed.component.App;
import app.packed.component.Bundle;
import app.packed.component.Wirelet;
import app.packed.container.ContainerState;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedBuildContext;
import packed.internal.component.PackedInitializationContext;

/**
 *
 */
// I don't think this works with passive systems...
// IDK

// Har kun noget der executor.....
// Og primaert mod at bliver kaldt fra main...

// Men meget af det taenker jeg skal styres af system wirelets...
// Maaske endda MainWirelets... (The first system???
public class Main {

    private Main() {}

    /**
     * This method will create and start an {@link App application} from the specified source. Blocking until the run state
     * of the application is {@link ContainerState#TERMINATED}.
     * <p>
     * Entry point or run to termination
     * 
     * @param bundle
     *            the source of the application
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application did not execute properly
     */
    // add exitOnEnter() <--- so useful for tests
    // exitable daemon...
    // https://github.com/patriknw/akka-typed-blog/blob/master/src/main/java/blog/typed/javadsl/ImmutableRoundRobinApp.java3
    public static void execute(Bundle<?> bundle, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedBuildContext.assemble(bundle, 0, null, wirelets);
        PackedInitializationContext.initialize(node);
    }

    // sync deamon???????
    // App.main(new Goo(), args);
    public static void main(Bundle<?> bundle, String[] args, Wirelet... wirelets) {
        ComponentNodeConfiguration node = PackedBuildContext.assemble(bundle, 0, null, Wirelet.combine(MainArgs.wireletOf(args), wirelets));
        PackedInitializationContext.initialize(node);
    }
}
