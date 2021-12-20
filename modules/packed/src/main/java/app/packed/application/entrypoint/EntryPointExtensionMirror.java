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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;

/**
 * A mirror for the {@link EntryPointExtension}.
 */
@ExtensionMember(EntryPointExtension.class)
public class EntryPointExtensionMirror extends ExtensionMirror implements Iterable<EntryPointMirror> {

    final EntryPointExtension e;

    final boolean isApplication;

    EntryPointExtensionMirror(EntryPointExtension e, boolean isApplication) {
        this.e = e;
        this.isApplication = isApplication;
    }

    public boolean isMain() {
        return e.hasMain;
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<EntryPointMirror> iterator() {
        return List.<EntryPointMirror>of().iterator();
    }

    public Optional<EntryPointMirror> main() {
        return Optional.empty();
    }

    public Class<? extends Extension> managedBy() {
        // There is always a single extension that manages all entry points in a single application
        // Fx
        //// CLI
        //// Serverless
        return EntryPointExtension.class;
    }

    public void overview() {}

    public void print() {
        // ------------------------ app.dd.EntryPointExtension ----------------------
        // EntryPoints
        /// --- DododoApppx
    }
}
