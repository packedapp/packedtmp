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
package app.packed.lifecycle;

import app.packed.container.Extension;

/**
 *
 */

// Skal have noget strategi her....
// Concurrent | Parallel

public final class LifecycleExtension extends Extension {

    /* package-private */ LifecycleExtension() {}

    public void foo() {}
    // Vi skal jo saaden set supportere det samme som wirelets...

    // wirelets callbacks er det foerend extension callbacks????

    // on(...)
    // on(..., Strategy strategy)
    public class Strategy {
        // Before all dependencies
        // After all dependencies
        // beforeExtension(....)
        // after Extension
        // before user code
        // after user code
    }

    public class Sub extends Subtension {
        /* package-private */ Sub() {}

        void onEnteringPostDependencies() {}

        void onEnteringPreDependencies() {

        }
    }

    // OnStart <---- will run in a virtual thread
}
