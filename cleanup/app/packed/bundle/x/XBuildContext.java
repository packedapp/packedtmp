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

/**
 *
 */
// What's the lifecycle of this faetter....
// Den er tilgaengelig fra Bundle.configure()....
// AppWiringContext, AppBootContext
// Vi kan jo godt klare os uden for the time being...
// WireContext
public class XBuildContext {

    public enum Stage {
        CREATE_RUNTIME, GENERATE_DESCRIPTOR, GENERATE_IMAGE;
    }

    // Allow fallback services in some way

    // fail on preview/experimental....
    // only allow stable bundles
    // Logging
    // Threads. startup shutdown

    // I forhold til environment

    // Rename to wireInjector, wireContainer

    // We do not need a namespace we can call the queue "messages" <-- and we can wire it via renameQueueImport

    // Require MessageConsumer

    // Ideen er at man kan f.eks. binde services.....
}

// So apps are a thing.....
// Environment... would also be something
