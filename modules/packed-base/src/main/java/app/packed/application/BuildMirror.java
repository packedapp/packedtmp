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

import app.packed.component.Assembly;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentMirrorStream;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.component.PackedComponentModifierSet;

/**
 * An assembly context is created every time an build context is create .
 * 
 * A build context is never available when we build something from an image. Or is it???
 */
// A build context is available via @Build IDK...
// Tror maaske ikke vi skal have andet end laese extensions.

// IDK about injecting it...
// It only works for injected it into build items...
// Or 

// Can faas fra extension context, Component Context.
// Men f.eks. addError() bliver noedt til at smide ISE hvis folk kalder metoder
// Efter assembly is done

// Nu naar vi kompilere en artifact med ind... kan vi jo ret set have en Artifact med
// Det eneste problem er hvis vi har host/guest... Saa er PodContext jo altsaa lidt bedre til at have det.

// addSuccessProcess(Runnable r);
// addFailureProcess(Runnable r);
// addCompletionProcesser(Consumer<@Nullable Throwable> d);

// Analysis?

// BuildTree / BuildSystem / 
// @ActiveService(phase=Building)

// Ideen er at du ikke fejler...

// Altsaa vi kalder det lidt et mirror...
// Fordi
public interface BuildMirror {

    // Maaske laver vi den til Optional...
    /** {@return the root component of the build}. */
    ApplicationMirror application();

    /** {@return the root component of the build}. */
    ComponentMirror component();

    /** {@return a component stream that includes every component in this build}. */
    ComponentMirrorStream components();

    // Whether or not we are building????
    boolean isDone();

    // Hmm saa skal vi jo til at lave builds i try/catch...
    boolean isFailed();

    boolean isSuccess();

    // Maaske vi hellere vil tilfoeje det lokalt???
    // F.eks. via Extension, eller assembly...

    // ServiceExtension.failed()

    // Throws IAE if post construct
    // Why would I add errors here???
    // Instead of lets say the extension?
    // void addError(ErrorMessage message);

    /**
     * Returns the set of modifiers used for this assembling.
     * <p>
     * The returned set will always contain the {@link ComponentModifier#BUILD} modifier.
     * 
     * @return a set of modifiers
     */
    // It is not nessesarily the system component. Just the top component of the assembling.
    ComponentModifierSet modifiers();

    BuildTarget target();

    // It can be on error path...
//    enum State {
//        IN_PROCESS, FAILED, SUCCES;
//    }
    

    /**
     * Builds an application(s) and returns a build object that can be used for further analysis.
     * 
     * @param assembly
     *            the main assembly of the application
     * @param wirelets
     *            optional wirelets
     * @return a build object
     */
    // Vi mangler noget omkring at fejle...
    // Altsaa taenker kun at det er specielle vaerktoejer der ikke vil fejle...
    // saa Maaske skal vi have 2 metoder
    // analyze -> ApplicationModel, build-> BuildModel???

    // buildForInstantiation(), buildImage
    // buildMirror?
    static BuildMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return PackedApplicationDriver.MIRROR_DRIVER.build(assembly, wirelets, PackedComponentModifierSet.I_MIRROR);
    }

}

//
///**
//* A root build is responsible for creating the root component of a system.
//* 
//* @return A system build is a root build that builds a system
//*/
//default boolean isRoot() {
//  return true;
//}

// Tror denne skaber mere forvirring end hjaelper
///**
//* Returns whether or not we are instantiating an actual artifact. Or if we are just producing an image or a descriptor.
//*
//* @return whether or not we are instantiating an actual artifact
//*/
//default boolean isInstantiating() {
//  return !(modifiers().contains(ComponentModifier.IMAGE) || modifiers().contains(ComponentModifier.ANALYSIS));
//}

// We could add ComponentPath path();
//// But it will freeze the name of the top level. Which we don't want.
