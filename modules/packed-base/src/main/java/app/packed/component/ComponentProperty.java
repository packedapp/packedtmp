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
import app.packed.container.ExtensionConfiguration;

/**
 * Component properties are permanent for a component
 */
// ComponentTag??
// Hmm taenker de er saa brugt at vi skal have en 
// ComponentProperties are fixed
public enum ComponentProperty {

    /**
     * Every system has exactly one system component which is the root of the component tree.
     */
    SYSTEM,

    /** */
    // Components this property are the component that are allowed to extensions as children.
    CONTAINER,

    /**
     * Indicates that the component represents a subclass of {@link Extension}.
     * <p>
     * Components with this property:
     * <ul>
     * <li>Always has a parent that has the {@link #CONTAINER} property set.</li>
     * <li>Are always leaf components (they have no children).</li>
     * <li>Are only present at runtime if they are part of an embedded image.</li>
     * <li>Has the {@link ExtensionConfiguration#TYPE} and {@link ExtensionConfiguration#EXTENSION_MEMBER} attribute set to
     * the type of the extension the component represents.</li>
     * </ul>
     */
    EXTENSION,

    /** */
    HOST,

    IMAGE,

    /**
     * Indicates that the component is an {@link Extension}.
     * <p>
     * Components with this property:
     * <ul>
     * <li>Are either the {@link System} component or has a parent that has the {@link #HOST} property set.</li>
     * <li>Are always leaf components (they have no children).</li>
     * <li>Are only present at runtime if they are part of an embedded image.</li>
     * <li>Has the {@link ExtensionConfiguration#TYPE} and {@link ExtensionConfiguration#EXTENSION_MEMBER} attribute set to
     * the type of the extension the component represents.</li>
     * </ul>
     */
    GUEST,

    SINGLETON,

    STATELESS;
}

//Components.isPartOfImage() <--- look recursively in parents and see if any has the Image 

enum Others {
    ARTIFACT, // FOREIGN???

    WIRELET, // Wirelet as in t I think instead it is a forerign component

    FOREIGN, // Wirelets, Artifacts are also FOREIGN...

    SYNTHETIC

    ,

    /**
     * A property indicating the component is a root
     */
    ROOT, // SYSTEM ???

    // RUNTIME <--- runtime wirelets. Not part of what can be static analysed???
    // Or maybe we have made room for provided services???? IDK det er lidt kompliceret...

    // Runtime
    // Buildtime???
    // Syntes de er lidt irriterende at have med altid
    // Ja det maa vaere en property paa system noden...
    // Image <-- part of an image, or the image root
    // Image, ImageRoot
}
// Guest -> Weakly linked.
// No-Guest -> Strongly linked

// Resource -> Something that can be shutdown???