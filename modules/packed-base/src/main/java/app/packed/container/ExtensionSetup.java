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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.statemachine.Leaving;

/**
 * An annotation that can be used on subclasses of {@link Extension}. Classes that extend {@link Extension} are implicit
 * sidecars even without the use of this annotation. However, if the extension uses any other extensions this annotation
 * must be used to indicate which extensions it may use.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
//app.packed.state.... Also ExtensionSidecar.STATE2_MAIN, ExtensionSidecar.STATE3_LINKING, ExtensionSidecar.STATE4_HOSTING

// dependencies = Class<? extends SubExtension>...
// implementation default Extension.class <-- must be fillout if extension is interface (extends Extension)...

// Skal vi dele dem op i mandatory og optional og optionalNamed
// Er mest taenkt for descriptors. F.eks. kan man se om en given extension er optional

// TODO we need a new name now that sidecars are no longer annotations
// Alternativ Settings
// ExtensionDependencies er vi vel tilbage i....

// transitive... Altsaa kan vi forstille os at extensions of extension skal bruge dem...

// Was ExtensionSettings
public @interface ExtensionSetup {

    /**
     * Used together with the {@link Leaving} annotation to indicate that an {@link Extension}method should be executed as
     * soon as the extension has been successfully instantiated and before it is returned to the user.
     * <p>
     * 
     * An extension sidecar event that the sidecar has been successfully instantiated by the runtime. But the instance has
     * not yet been returned to the user. The next event will be {@link #NORMAL_USAGE}.
     */
    String INSTANTIATING = "Instantiating";

    /**
     * All components and extensions have been added and configured. The next event will be {@link #CHILD_LINKING}
     */
    String NORMAL_USAGE = "NormalUsage";

    /**
     * Any child containers located in the same artifact will be has been defined. Typically using . The next event will be
     * {@link #GUESTS_DEFINITIONS}.
     */
    String CHILD_LINKING = "ChildLinking";

    /** This is the final event. This event will be invoked even if no guests are defined. */
    String GUESTS_DEFINITIONS = "GuestsDefinitions";

    /** The end state of the extension. */
    String ASSEMBLED = "Assembled";

    /**
     * Other extensions that an extension may use (but do not have to). This need not include transitive dependencies
     * (dependencies of dependencies). Only extensions that are directly used, for example, via
     * {@link ExtensionConfiguration#use(Class)}.
     * 
     * @return extensions that the extension may use
     */
    // Should we use Sub instead??? Giver god mening.. Da man ikke kan depend paa extensions der ikke har en sub
    Class<? extends Extension>[] dependencies() default {};

    /**
     * Other extensions that an extension may use if they are present on the classpath or modulepath.
     * <p>
     * The extension types will only be used if they can be resolved at runtime using
     * {@link Class#forName(String, boolean, ClassLoader)} or a similar mechanism.
     * <p>
     * Checking whether or not an optional dependency is available is done exactly once when the extension is first used.
     * Caching the result for future usage.
     * 
     * @return extensions that the extension may use if they are present on the classpath or modulepath
     */
    String[] optionalDependencies() default {};

}
//
///**
//* Returns any runtime sidecar representations.
//* 
//* @return any runtime sidecar representations
//*/
//// wtf is this
//// Ahh, det maa vaere en ide om at vi skal definere runtime componenterne foer vi bruger dem...
//// Vi dropper det...
//Class<?>[] runtime() default {};