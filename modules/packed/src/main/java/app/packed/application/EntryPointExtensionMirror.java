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
package app.packed.application;

import java.util.Iterator;
import java.util.Optional;

import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;

/**
 *
 */
public class EntryPointExtensionMirror extends ExtensionMirror implements Iterable<EntryPointMirror> {

    /** {@inheritDoc} */
    @Override
    public Iterator<EntryPointMirror> iterator() {
        throw new UnsupportedOperationException();
    }

    public Class<? extends Extension> invokedBy() {
        // There is always a single extension that manages all entry points in a single application
        // Fx
        //// CLI
        //// Serverless
        return EntryPointExtension.class;
    }

    public Optional<EntryPointMirror> main() {
        return Optional.empty();
    }

    public boolean hasMain() {
        return invokedBy() == EntryPointExtension.class;
    }

    public void overview() {}

    public void print() {
        // ------------------------ app.dd.EntryPointExtension ----------------------
        // EntryPoints
        /// --- DododoApppx
    }

    public static void main(String[] args) {
        for (EntryPointMirror m : ApplicationMirror.of(new MyAss()).use(EntryPointExtensionMirror.class)) {
            System.out.println(m);
        }
        ApplicationMirror.of(new MyAss()).use(EntryPointExtensionMirror.class).print();
    }

    static class MyAss extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {}
    }
}
