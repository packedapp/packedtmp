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

import java.lang.reflect.Modifier;

import app.packed.artifact.App;
import app.packed.artifact.Image;
import app.packed.artifact.Main;
import app.packed.container.Extension;
import app.packed.lifecycle.AssemblyContext;

/**
 * A component modifier indicates a permanent property of a component.
 * <p>
 * Modifiers are typically returned in a {@link ComponentModifierSet}.
 * <p>
 * The order in which these modifier are defined may change from release to release.
 * 
 * @apiNote Packed uses a enum modifier similar to how Java uses {@link Modifier} to indicate access properties of
 *          members and types. The alternative would be
 */
public enum ComponentModifier {

    /**
     * Indicates that the system is in the assembly phase.
     * <p>
     * When a assembled system is initialized. A new system is created retained the structure of the assembled system but
     * without this modifier.
     * <p>
     * A system that has the {@link #IMAGE} modifier set is always in an assembled state.
     * <p>
     * The modifier set returned by {@link AssemblyContext#modifiers()} always contain this modifier.
     **/
    ASSEMBLY,

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
     * Indicates that the component holds an image.
     * 
     * Components with this modifier:
     * <ul>
     * <li>Always has a parent component with the {@link #ASSEMBLY} modifier set.</li>
     * <li>Either have the {@link #SYSTEM} modifier set, or has a parent component with {@link #HOST} modifier set.</li>
     * <li>The subtree of an image is always immutable once constructed. A {@link #HOST} modifier on a sub component. Merely
     * indicates that a runtime spawn of the image can add guests.</li>
     * </ul>
     */
    IMAGE,

    /**
     * Indicates that the component is a container.
     * 
     * <p>
     * * Components with this modifier:
     * <ul>
     * <li>Are allowed to have children with the {@link #EXTENSION} modifier set.</li>
     * <li>Are never sourced???.</li>
     * </ul>
     */
    CONTAINER,

    /**
     * Indicates that the component represents a subclass of {@link Extension}.
     * <p>
     * Components with this modifier:
     * <ul>
     * <li>Always has a parent component with the {@link #CONTAINER} modifier set.</li>
     * <li>Are always leaf components (they have no children).</li>
     * <li>Are only present at runtime in a system if it is part of an {@link Image}.</li>
     * <li>Never has any other modifiers set.</li>
     * <li>Has the {@link ComponentAttributes#EXTENSION_MEMBER} attribute set to the type of the extension the component
     * represents.</li>
     * </ul>
     */
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

    /**
     * Indicates that a component has a shell attached that can interact with it. One such examples, is by using
     * {@link App#initialize(Bundle, Wirelet...)} to create a shell.
     * <p>
     * Shells that are attached to a guest are co-terminus with the guest. Restarting the guest will create a new shell. And
     * users make
     * <p>
     * Systems that are created via the various methods on {@link Main} never has a shell.
     */
    SHELL, // FOREIGN???

    /**
     * Indicates that a system has been created for the sole reason of being analyzed is some way. A system with this
     * modifier will never go through its initialization phase. Extensions may use this information to avoid work that is
     * not needed if the system is never initialized.
     * <p>
     * This modifier is normally checked in the assembly phase via {@link AssemblyContext#modifiers()}.
     * <p>
     * The modifier is set by the various methods in {@link ComponentAnalyzer} when specifying a bundle. Systems that are
     * already running will not have this modifier set when they are analysed.
     */
    ANALYSIS,

    /**
     * Indicates that a component is added by the runtime but is not explicitly or implicitly declared by the user.
     * <p>
     * A good example is the an artifact. The user itself does not add this component.
     * <p>
     * Server a similar purpose as Java's synthetic access modifier.
     */
    // Maaske er alle foreign components, synthetiske...
    // Taenker ihvertfald ikke det er noget med specifikt tilfoejer...
    SYNTHETIC,

    // A single java based instance that is strongly bound to lifecycle of the component.
    // Cannot be replaced. As such this instance is co-terminus with the guest
    SINGLETON,

    // Stateless, but we inject new stuff...
    UNSCOPED;

    public ComponentModifierSet toSet() {
        return ComponentModifierSet.of(this);
    }

    int bits() {
        return 1 << ordinal();
    }
}

//Components.isPartOfImage() <--- look recursively in parents and see if any has the Image 

enum Sandbox {

    NATIVE_IMAGE, // if built using GRAAL

    JOB,

    TASK,

    REQUEST,

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

// Should extension have the source set...
// {@link ComponentAttributes#SOURCE_TYPE} var tidligere sat...
// Men syntes det er en runtime ting... Og angiver at man bliver analyseret...
// Nej syntes ikke den skal have SOURCE_TYPE sat, med mindre den har en source...
//ComponentTag??
//Hmm taenker de er saa brugt at vi skal have en 
//ComponentProperties are fixed
//ComponentRole? SYNTHETIC/FOREIGN/ARTIFACT er en daarlig rolle

//ComponentModifier??? They are pretty similar
//Components can have more than 1, they are fixed once constructed
//certain combinations don't mix
//Altsaa det minder mere om modifiers. 
//Properties plejer at vaere key value
//Modifier er ogsaa mere forskelligt fra attribute end property 
