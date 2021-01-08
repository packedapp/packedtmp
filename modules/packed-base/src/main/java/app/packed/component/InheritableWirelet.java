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
 *
 */
// Ideen er at man som default ikke bliver inherited...
// Men at man kan

// I selektionen minder den meget om component stream...

// Folk faar den oprindelige wirelet...
// Saa inheritable maa 100% styres af Packed

// 
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

    public final Wirelet forExtension(Class<? extends Extension> extensionType) {
        throw new UnsupportedOperationException();
    }
    
    public final Wirelet assembledBy(Class<? extends Assembly<?>> assemblyType) {
        throw new UnsupportedOperationException();
    }
    
    public final Wirelet all() {
        throw new UnsupportedOperationException();
    }
    
    public final Wirelet sameContainer() {
        throw new UnsupportedOperationException();
    }
    
    public final InheritableWirelet maxDepth(int maxDeptch) {
        return this;
    }
    
    public final Wirelet matchPath(String regexp) {
        throw new UnsupportedOperationException();
    }
    
    public final Wirelet allExceptThis() {
        throw new UnsupportedOperationException();
    }
}
