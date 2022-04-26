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
package app.packed.application.entrypoint;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;

/**
 * A mirror for the {@link EntryPointExtension}.
 */
public class EntryPointExtensionMirror extends ExtensionMirror<EntryPointExtension> {

    /* package-private */ EntryPointExtensionMirror() {}

    /**
     * @return
     * 
     * @see Main
     */
    public boolean hasMain() {
        return tree().stream().anyMatch(e -> e.hasMain);
    }

    /** {@inheritDoc} */
    public Collection<EntryPointMirror> entryPoints() {
        // Man boer jo kunne extende dem EntryPoints....
        // Altsaa hvis jeg bruge CliExtension...
        return List.of();
    }

    /**
     * @return stuff
     */
    public Optional<EntryPointMirror> main() {
        return Optional.empty();
    }

    // Optional, I think. We can add the extension
    // But add any entry points
    public Class<? extends Extension<?>> managedBy() {
        // There is always a single extension that manages all entry points in a single application
        // Fx
        //// CLI
        //// Serverless
        return tree().root().shared().takeOver;
    }

    public void print() {
        // ------------------------ app.dd.EntryPointExtension ----------------------
        // EntryPoints
        /// --- DododoApppx
    }
}
