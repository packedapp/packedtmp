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

import java.util.Set;

import app.packed.container.Extension;
import packed.internal.component.PackedComponentModifierSet;

/**
 * Component properties are permanent for a component
 */
// ComponentTag??
// Hmm taenker de er saa brugt at vi skal have en 
// ComponentProperties are fixed
// ComponentRole? SYNTHETIC/FOREIGN/ARTIFACT er en daarlig rolle

// ComponentModifier??? They are pretty similar
// Components can have more than 1, they are fixed once constructed
// certain combinations don't mix
// Altsaa det minder mere om modifiers. 
// Properties plejer at vaere key value
// Modifier er ogsaa mere forskelligt fra attribute end property 
public enum ComponentModifier {

    ASSEMBLY, // Not a runtime-system

    // System wide.. what is part of the system and what is part of the environment
    // System boundary
    // Bondary vs Environment...
    // Maybe Environment is bad because of overloaded meaning
    ENVIRONMENT, // Wirelets, Artifacts are also FOREIGN or EXTERNAL...

    /**
     * Every component system has exactly one system component which is always the root of the component tree.
     * 
     * @see Component#system()
     */
    // Always a guest. Naah, hvad hvis vi har system view...????
    // Was This component is also always automatically a {@link #GUEST}.
    SYSTEM,

    /**
     * Indicates that the component is the root of an image.
     */
    // An image always either has a host as a parent
    // Or is the root
    IMAGE,

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
     * <li>Has the {@link ComponentAttributes#EXTENSION_MEMBER} attribute set to the type of the extension the component
     * represents.</li>
     * </ul>
     */
    // {@link ComponentAttributes#SOURCE_TYPE} var tidligere sat...
    // Men syntes det er en runtime ting... Og angiver at man bliver analyseret...
    // Nej syntes ikke den skal have SOURCE_TYPE sat, med mindre den har en source...
    EXTENSION,

    /**
     * Indicates that a component can have children added outside of the assembly phase.
     * 
     * Components that are not hosts cannot have children added outside of its assembly phase.
     */
    HOST,

    /**
     * Indicates that the component is an {@link Extension}.
     * <p>
     * Components with this property:
     * <ul>
     * <li>Are either the {@link System} component or has a parent that has the {@link #HOST} property set.</li>
     * <li>Are always leaf components (they have no children).</li>
     * <li>Are only present at runtime if they are part of an embedded image.</li>
     * <li>Has the {@link ComponentAttributes#SOURCE_TYPE} and {@link ComponentAttributes#EXTENSION_MEMBER} attribute set to
     * the type of the extension the component represents.</li>
     * </ul>
     */
    GUEST,

    FUNCTION,

    /**
     * The component is created on the basis of an instance, a class or a factory.
     * 
     * {@link ComponentAttributes#SOURCE_TYPE} is set with class source.
     */
    SOURCED, // All components that are sourced have an SOURCE_TYPE attribute
    // SOURCED_TYPE... We don't need EXTENSION TYPE THEN????

    SHELL, // FOREIGN???

    /**
     * Indicates that a system has been created for the sole reason of being analyzed. The system will never move from the
     * assembly phase.
     * 
     * This modifier is set when using any of the methods on Analysis. Extensions may use this information to avoid invoking
     * processes that are only needed if the extension is to be instantiated. Or keep extra information...
     * 
     * I think it is only available for the root/system component...
     * 
     * Nej systes sagtens man have en analyze(Bundle) paa en host
     */
    ANALYSIS,

    /**
     * Indicates that the component has been added by the runtime.
     * <p>
     * A good example is the an artifact. The user itself does not add this component.
     * 
     */
    SYNTHETIC,

    // A single java based instance that is strongly bound to lifecycle of the component.
    // Cannot be replaced. As such this instance is co-terminus with the guest
    SINGLETON,

    // Stateless, but we inject new stuff...
    UNSCOPED;

    // THESE METHODS DO NOT GUARANTEE to return the same int across versions
    // Eneste problem er, lad os nu sige vi lige pludselig faar mere en 32 properties...
    // Hvilket jeg ikke regner med er realistisk, men vi har lige pludselig exposed det
    // i vores API.
    public static int toBits(ComponentModifier p) {
        return 1 << p.ordinal();
    }

    public static int toBits(ComponentModifier p1, ComponentModifier p2) {
        return 1 << p1.ordinal() + 1 << p2.ordinal();
    }

    public static Set<ComponentModifier> fromBits(int bits) {
        throw new UnsupportedOperationException();
    }

    public static Set<ComponentModifier> setOf() {
        return Set.of();
    }

    public static Set<ComponentModifier> setOf(ComponentModifier m) {
        return new PackedComponentModifierSet(toBits(m));
    }
}

//Components.isPartOfImage() <--- look recursively in parents and see if any has the Image 

enum Sandbox {

    // IDK if we will ever use it... But just a reminder.
    EPHEMERAL, // https://kubernetes.io/docs/concepts/workloads/pods/ephemeral-containers/

    // A single function / single instance??
    // FUNCTION,

    WIRELET, // Wirelet as in t I think instead it is a forerign component

    /**
     * A property indicating the component is a root
     */
    // ROOT, // SYSTEM ???

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