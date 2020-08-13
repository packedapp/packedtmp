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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.artifact.ArtifactImage;
import packed.internal.component.wirelet.WireletModel;

/**
 * An annotation that can be used on subclasses of {@link Wirelet}. Classes that extend {@link Wirelet} are implicit
 * sidecars even without the use of this annotation. However, if the wirelet is part of a pipeline this must be
 * indicated by using this annotation.
 * 
 * @apiNote this annotatino will have Inherited removed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

@Inherited
// see for example ServiceWirelet, should be enough just to have a test for it...
// I'm not sure this should be inherited because @ExtensionMemberType is not...
// If this is inherited. I think all sidecar annotations should be inherited...
// ExtensionWireletMeta
public @interface WireletSidecar {

    /**
     * 
     * @return a pipeline
     */
    Class<? extends WireletPipeline<?, ?>> pipeline() default WireletModel.NoWireletPipeline.class;

    /**
     * Returns whether or not the wirelet is needed at assembly time. In which in cannot used together with a
     * {@link ArtifactImage} that have already been constructed. However, it can be used when constructing the image.
     * 
     * @return stuff
     */
    // Taenker paa at vende den om...Saa vi som default kun kan bruges paa AssemblyTime...
    // Nej fordi user er altid 100% ligegyldig. Eftersom det aldrig eksekvere...
    // Bundle??? inject Wirelets

    // Must be specified when On Image creation time.

    // This setting is primary used by extensions that define wirelets that can only be used at assembly time
    // Navnet er daarligt syntes jeg.. Syntes det hurtigt begyndet at bliver kompliceret hvis vi bruger expand.
    // AssemblyTime.. Ikke grund til at introduce expand....
    // require

    // Fails if the annotated wirelet is used on an existing image
    // failForImage
    boolean failOnImage() default false;
}

///**
//* Whether or not a specified wirelet is inherited by child containers. The default value is <code>false</code>.
//* 
//* @return whether or not the wirelet is inherited by child containers
//*/
//boolean inherited() default false;

// failIfExtensionUnavailable default true();
// ArtifactWirelets... Wirelets that cannot be used for linkage...
// For example, timeToLive...
// Maybe also system Wirelets.. Only the root... For example, App.of(SystemWirelets.AddShutdownHook());

// Hvis vi har behov for at differentiere mellem artifact og system...
// Lav det som en inner class i WireletSidecar
enum Inheritance {
    NONE, ARTIFACT, SYSTEM;
}

//Altsaa public klasser boer nok provide as service...
// boolean requireAssemblyTime() must be used on assembly time
// Cannot be used on an image after it has been created

// assembleOnly
// linkOnly
// hostOnly
