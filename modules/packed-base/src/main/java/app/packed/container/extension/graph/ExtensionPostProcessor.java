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
package app.packed.container.extension.graph;

import app.packed.container.extension.Extension;

/**
 *
 */
// iterative graph processing

// Ideen er at vi laver en per extension-per container

// Process bliver saa ved

// Call into super artifact...
public abstract class ExtensionPostProcessor<E extends Extension> {

    /**
     * Returns the number of extensions in the same artifact.
     * 
     * @return the number of extensions in the same artifact
     */
    public int count() {
        return 0;
    }

    protected E root() {
        // Er jo super nyttig hvis man er viral
        throw new UnsupportedOperationException();
    }

    protected abstract void process();
}
