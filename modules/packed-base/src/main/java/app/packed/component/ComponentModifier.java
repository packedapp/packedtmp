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

import app.packed.cli.Main;
import app.packed.cube.Extension;
import app.packed.inject.Factory;

/**
 * A component typically has one or modifiers that indicates a permanent property of a component.
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
     * Every component system has exactly one system component which is always the root of the component tree.
     * 
     * @see Component#system()
     */
    // Always a guest. Naah, hvad hvis vi har system view...????
    // Was This component is also always automatically a {@link #GUEST}.
    SYSTEM,

    /**
     * Indicates that the component and all of its children is in the assembly phase. When such a system is initialized. A
     * new system is created retained the structure of the assembled system but without this modifier.
     * <p>
     * A system that has the {@link #IMAGE} modifier set is always in an assembled state.
     * <p>
     * The modifier set returned by {@link BuildContext#modifiers()} always contain this modifier.
     **/
    // BUILDING?
    BUILD,

    // System wide.. what is part of the system and what is part of the environment
    // System boundary
    // Bondary vs Environment...
    // Maybe Environment is bad because of overloaded meaning
    /**
     * Are components that should not be considered part of the system. But are nonetheless present in order to XXX.
     * 
     * A typical example is wirelets specified when starting a system. As they have been provided from the outside... Only
     * root wirelets????
     * 
     * Another example
     * 
     */
    // Hmm, hvad med en exstern database????
    EXTERNAL, // Wirelets, Artifacts are also FOREIGN or EXTERNAL...ENVIRONMENT

    /**
     * Indicates that the component holds an image.
     * 
     * Components with this modifier:
     * <ul>
     * <li>Always has a parent component with the {@link #BUILD} modifier set.</li>
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
    CUBE,

    /**
     * Indicates that the component is a part of an extension.
     * <p>
     * Components with this modifier:
     * <ul>
     * <li>Always has a parent component with the {@link #CUBE} modifier set.</li>
     * <li>Are always leaf components (they have no children).</li>
     * <li>Are only present at runtime in a system if it is part of an {@link Image}.</li>
     * <li>Never has any other modifiers set.</li>
     * <li>Has the {@link ComponentAttributes#EXTENSION_MEMBER} attribute set to the type of the extension the component
     * represents.</li>
     * </ul>
     */
    // Altsaa maaske betyder det bare at noget kommer fra en Extension??? Ogsaa paa runtime
    // If Extension_Member = SOURCE
    // Ja det super meget lettere at filtere
    // SOURCE_TYPE == EXTENSION_MEMBER_TYPE -> Assembly time extension
    // Reposity JPA generated component er ikke en extension, selvom det maaske er saadan en der
    // tilfoejer den
    EXTENSION,

    /**
     * Indicates that a component can have children added outside of the assembly phase.
     * 
     * Components that are not hosts cannot have children added outside of its assembly phase.
     */
    HOST,

    /**
     * Indicates that the component represents a subclass of {@link Extension}.
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
    CONTAINER,

    /**
     * Indicates that the components wraps a single abstract method (SAM).
     * 
     * FunctionType is type FunctionDescriptor??? istedet for bare en klasse??? Ja taenker den skal tage info fra dem der
     * har lavet en FunctionComponentType...
     * 
     * I think it always have the source type set...
     */
    FUNCTION,

    /**
     * Indicates that the user has provided a source (Object instance, a {@link Class} or a {@link Factory}) when installing
     * the component.
     * 
     * Components with this property:
     * <ul>
     * <li>Has the {@link ComponentAttributes#SOURCE_TYPE} set to the type used when registrating the component.</li>
     * </ul>
     * If we are class generating something this also needs to be a source. As source_type indicates that the type that
     * should be analyzed
     * <p>
     * What about function??? if we support annos paa functions we should have source type
     */
    SOURCED,

    /**
     * Indicates that a component has a shell attached. For example, an application created via
     * {@link App#of(Bundle, Wirelet...)} to create a shell.
     * <p>
     * Shells that are attached to a guest are co-terminus with the guest. Restarting the guest will create a new shell. And
     * users make
     * <p>
     * Systems that are created via the various methods on {@link Main} never has a shell.
     * 
     * @see App
     * @see ShellDriver
     */
    SHELL, // FOREIGN???

    /**
     * Indicates that a system has been created for the sole reason of being analyzed. A system with this modifier will
     * never go through any initialization phase. Extensions may use this information to avoid work that is not needed if
     * the system is never initialized.
     * <p>
     * This modifier is typically checked by accessing {@link BuildContext#modifiers()}, for example, via
     * {@link Extension#assembly()}.
     * <p>
     * The modifier is set by the various methods in {@link ComponentAnalyzer} when specifying a bundle. Systems that are
     * already running will not have this modifier set when they are analysed.
     * 
     * Components with this property:
     * <ul>
     * <li>Always have the {@link #BUILD} modifier set as well.</li>
     * <li>Are never present at runtime.</li>
     * </ul>
     * 
     * @see ComponentAnalyzer
     */
    ANALYSIS,

    /**
     * Indicates that the system is PASSIVE. Components with this property:
     * 
     * Indicates that once the system is constructed it will never change.
     * 
     * <ul>
     * <li>Always have the {@link #SYSTEM} modifier set as well.</li>
     * <li>Never has a parent component (is root).</li>
     * <li>Never has any components with the {@link #HOST} modifier set.</li>
     * </ul>
     * A system is always either stable or a guest.
     */
    PASSIVE, // Bliver vi naesten noedt til at have Active ogsaa..

    /**
     * Indicates that the component has been not explicitly or implicitly installed by the user.
     * <p>
     * A typical example is components installed by the extensions that (or are they just hidden???)
     * 
     * been added by the runtime but was .
     * <p>
     * A good example is the an artifact. The user itself does not add this component.
     * <p>
     * Components with this modifier are typically filtered.
     * <p>
     * This modifier serves a similar purpose to Java's synthetic access modifier.
     */
    // Maaske er alle foreign components, synthetiske...
    // Taenker ihvertfald ikke det er noget med specifikt tilfoejer...
    // Hmm hvad med extensions componeter????? Syntes maaske ikke de er syntetiske... IDK
    SYNTHETIC,

    // A single java based instance that is strongly bound to lifecycle of the component.
    // Cannot be replaced. As such this instance is co-terminus with the guest
    // If an instance is specified it is used, otherwise a new instance will created
    // from the class of factory.

    // install(Ffff.class); <-----
    // install(Ffff.class); <-----
    // Creates two singletons of the same type

    // https://www.powerthesaurus.org/permanent/synonyms
    // Constant???
    // Instant
    CONSTANT, // passer ogsaa bedre med provide

    /**
     * Something that needs to be managed by the runtime after it has been initialized.
     * <p>
     * Since Java is GC'ed we do not have to worry about ordinary objects...
     */
    STATEFUL, // Something that has to be managed by the runtime after it has been initialized.

    STATELESS, // passer ogsaa bedre med provide

    UNSCOPED; // Det er her hvor den kan vaere managed eller unmanaged..

    /**
     * Returns a component modifier set containing only this modifier.
     * 
     * @return a component modifier set containing only this modifier
     */
    public ComponentModifierSet toSet() {
        return ComponentModifierSet.of(this);
    }

    int bits() {
        return 1 << ordinal();
    }
}

//Components.isPartOfImage() <--- look recursively in parents and see if any has the Image 

enum Sandbox {

    FOREIGN, // A non-JVM language...

    INJECTABLE, // Syntes denne er daarlig fordi det er foerst noget vi ved efter
    // sourcen er bundet. provide(Class) -> INJECTABLE, provide(instance) -> NOT_INJECTABLE

    RESTARTABLE,

    AOT,

    /**
     * Indicates that parts of the component has been generated.
     * <p>
     * The source was generated (GENERATED_FROM attribute???? nah. RepositoryType = ... fx for JPA
     */
    GENERATED,

    SPAWNED, // ??? Injector from an injector?? IDK can't think of a usercase...

    STABLE, // Not a guest... (Immobile)

    NATIVE_IMAGE, // if built using GRAAL

    JOB, // Why not just an Executor Service??? Because we provide services to the job...
    // A job provides a result??? Maybe a tracker?

    TASK, // I don't think Task... A job it split into tasks
    // Tasks are not present in the system

    REQUEST,

    // IDK if we will ever use it... But just a reminder.
    EPHEMERAL, // https://kubernetes.io/docs/concepts/workloads/pods/ephemeral-containers/

    UNSAFE, // Components that use Unsafe is marked paa en frivillig basis...

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
