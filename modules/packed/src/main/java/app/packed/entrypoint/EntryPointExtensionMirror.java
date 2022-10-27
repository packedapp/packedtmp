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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;

/** A specialized extension mirror for the {@link EntryPointExtension}. */
public final class EntryPointExtensionMirror extends ExtensionMirror<EntryPointExtension> {

    /* package-private */ EntryPointExtensionMirror() {}

    // Optional, I think. We can add the extension
    // But add any entry points
    public Optional<Class<? extends Extension<?>>> dispatcher() {
        // There is always a single extension that manages all entry points in a single application
        // Fx
        //// CLI
        //// Serverless
        return Optional.ofNullable(navigator().root().shared.dispatcher);
    }

    public Collection<EntryPointMirror> entryPoints() {
        // Vi behoever jo strengt taget ikke selv holde styr paa dem
        // OperationMirror.findAllOperationsAssingableTo(EntryPointOperationMirror);
        
        // Man boer jo kunne extende dem EntryPoints....
        // Altsaa hvis jeg bruge CliExtension...
        return List.of();
    }

    /**
     * @return
     * 
     * @see Main
     */
    public boolean hasMain() {
        return allAnyMatch(e -> e.hasMain);
    }

    /**
     * @return stuff
     */
    public Optional<EntryPointMirror> main() {
        return Optional.empty();
    }

    public void print() {
        // ------------------------ app.dd.EntryPointExtension ----------------------
        // EntryPoints
        /// --- DododoApppx
    }
}
