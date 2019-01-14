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
package app.packed.app;

/**
 *
 */

// So apps are a thing.....
// Environment... would also be something

public class AppConfigurationContext {

    // Allow fallback services in some way

    // fail on preview/experimental....
    // only allow stable bundles
    // Logging
    // Threads. startup shutdown

    public static void main(String[] args) {
        System.out.println("HelloWorld");
        // Injector.of(c -> {
        // c.bind("foo");
        // });
    }

    // I forhold til environment
    public enum Stage {
        CREATE_RUNTIME, GENERATE_DESCRIPTOR, GENERATE_IMAGE;
    }

    // Rename to wireInjector, wireContainer

    // We do not need a namespace we can call the queue "messages" <-- and we can wire it via renameQueueImport

    // Require MessageConsumer

    // Ideen er at man kan f.eks. binde services.....

    // App.launch(Class<? extends Bundle>,
}
