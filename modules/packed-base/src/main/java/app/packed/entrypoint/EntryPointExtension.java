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
package app.packed.entrypoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import app.packed.container.Bundle;
import app.packed.container.Extension;
import app.packed.inject.InjectionExtension;
import app.packed.util.Key;

/**
 * 
 * If an artifact does not contain any entry points. The artifact will, once started, run until it shutdown by the user.
 * 
 * <p>
 * Containers that link other containers with this extension will automatically have the extension installed.
 * <p>
 * This extension or {@link Main} is not supported at runtime.
 */
// Possibilities
// No Main/Cli -> Daemon, will run until shutdown/terminated
// Main -> Will run until main method exists in some way...
// Cli -> Will run one of the cli endpoints, which can either be daemons, or run til end.

// Do we require lifecycle extension???? Nahhh

// Implementation.. look and see if EntryPointExtension exists... and run it...
// Otherwise you are a daemon

// Other Stuff
//// Handling of Main exceptions??? Naah ErrorHandlingExtension
//// What if result?????? have some of Container methods return Object???
public final class EntryPointExtension extends Extension {

    /** Creates a new entry point extension. */
    EntryPointExtension() {}

    public <T> void main(Key<T> serviceKey, Consumer<? super T> consumer) {
        // invocation multiple times??? Error?
        // What if we have a @Main method? override. What about the dependencies
        // from the @Main method???
        use(InjectionExtension.class).addRequired(serviceKey);
        // How does this work implementation wise??
        // We call InjectionExtension.require(serviceKey) (Which backtraces stackwalker)
    }

    public <T> void main(Class<T> serviceKey, Consumer<? super T> consumer) {
        main(Key.of(serviceKey), consumer);
    }

    /** {@inheritDoc} */
    @Override
    protected void onConfigured() {
        installInParentIfSameArtifact();
    }
}

class X extends Bundle {

    @Override
    public void configure() {
        use(EntryPointExtension.class).main(ConcurrentHashMap.class, c -> System.out.println("size = " + c.size()));
    }
}

// @Map(PicoClicc. EntryPointExtension)
// ....
@interface UsesPicoCli {}