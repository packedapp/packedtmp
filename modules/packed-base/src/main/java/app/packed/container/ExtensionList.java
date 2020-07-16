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
package app.packed.container;

import java.util.stream.Stream;

/**
 *
 */
// To Graph...

//Class vs Descriptor

// toClassList();, toDescriptorList();c

// Graph<ExtensionDescriptor, Void>

// Does ED only contain actual used dependencies????
// IDK... Maybe we only have classes???
// Problem is we need to retain info about inter extension
// relationships at runtime.

// specials --> ExtensionX before ExtensionY

// ExtensionList because well we have an order...
public interface ExtensionList {

    /**
     * Returns whether or not this set contains the specified extension type.
     * 
     * @param extensionType
     *            the extension type to test
     * @return whether or not this set contains the specified extension type
     */
    boolean contains(Class<? extends Extension> extensionType);

    Stream<ExtensionDescriptor> descriptors();

    /**
     * Returns whether or not this set contains any extensions.
     * 
     * @return whether or not this set contains any extensions
     */
    boolean isEmpty();

    /**
     * Returns the number of extensions.
     * 
     * @return the number of extensions
     */
    int size();

    static ExtensionList empty() {
        throw new UnsupportedOperationException();
    }
}
