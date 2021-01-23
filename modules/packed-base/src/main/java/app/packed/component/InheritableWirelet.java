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
package app.packed.component;

import app.packed.container.Extension;

/**
 * A wirelet
 */
// Ideen er at man som default ikke bliver inherited...
// Men at man kan

// I selektionen minder den meget om component stream...

// Folk faar den oprindelige wirelet...
// Saa inheritable maa 100% styres af Packed

// Skal lige finde ud af omkring build-time/run-time
public abstract class InheritableWirelet extends Wirelet {

    /**
     * 
     */
    public InheritableWirelet() {
        super();
    }

    /**
     * @param modifier
     */
    public InheritableWirelet(ComponentModifier modifier) {
        super(modifier);
    }

    public final InheritableWirelet assembledBy(Class<? extends Assembly<?>> assemblyType) {
        throw new UnsupportedOperationException();
    }

    public final InheritableWirelet forExtension(Class<? extends Extension> extensionType) {
        throw new UnsupportedOperationException();
    }

    public final InheritableWirelet ignoreThis() {
        throw new UnsupportedOperationException();
    }

    public final InheritableWirelet matchPath(String regexp) {
        throw new UnsupportedOperationException();
    }

    public final InheritableWirelet maxDepth(int maxDeptch) {
        return this;
    }

    public final InheritableWirelet sameContainer() {
        throw new UnsupportedOperationException();
    }

    /**
     * By default an inheritable wirelet will be inherited
     * 
     * @return the wirelet
     */
    // Maybe just call it wirelet()???
    // We simply just say that you cannot change how it is inherited
    public final Wirelet wirelet() {
        throw new UnsupportedOperationException();
    }
}
