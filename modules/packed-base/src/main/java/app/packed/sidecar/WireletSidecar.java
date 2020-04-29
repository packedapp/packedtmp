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
package app.packed.sidecar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.artifact.SystemImage;
import app.packed.container.Wirelet;
import app.packed.container.WireletPipeline;
import packed.internal.container.WireletModel;

/**
 * An annotation that can be used on subclasses of {@link Wirelet}. Classes that extend {@link Wirelet} are implicit
 * sidecars even without the use of this annotation. However, if the wirelet is part of a pipeline this must be
 * indicated by using this annotation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited // see for example ServiceWirelet
public @interface WireletSidecar {

    /**
     * Whether or not a specified wirelet is inherited by child containers. The default value is <code>false</code>.
     * 
     * @return whether or not the wirelet is inherited by child containers
     */
    boolean inherited() default false;

    Class<? extends WireletPipeline<?, ?>> pipeline() default WireletModel.NoWireletPipeline.class;

    // I think a boolean is fine. Can't imaging you would want to expose it as any other type as the annotated type..
    boolean provideAsService() default false;

    /**
     * Returns whether or not the wirelet is needed at assembly time. In which in cannot used together with a
     * {@link SystemImage} that have already been constructed. However, it can be used when constructing the image.
     * 
     * @return stuff
     */
    boolean requireAssemblyTime() default false;
}

// Hvis vi har behov for at differentiere mellem artifact og system...
// Lav det som en inner class i WireletSidecar
enum Inheritance {
    NONE, ARTIFACT, SYSTEM;
}

//Vi slipper af med PipelineWirelet
//Det er lettere at override den for subclasses...
//F.eks. MainArgs implements Wirelet... Men altsaa hvis vi kan injecte den... via @ProvideWirelet
//Behoever vi saa service???? IDontThinkSo...
//@ProvideWirelet kan ogsaa f.eks. go deeper... f.eks. ind i andre containere i wireletten

//Altsaa for Conf giver ret god mening... Vi siger vi skal have en Conf...

//Altsaa public klasser boer nok provide as service...
// boolean requireAssemblyTime() must be used on assembly time
// Cannot be used on an image after it has been created

// assembleOnly
// linkOnly
// hostOnly

// vil automatisk bliver provided som service
@WireletSidecar(provideAsService = true)
class XDoofar implements Wirelet {

}

// Must use Optional/Nullable for wirelet
// Works for both wirelets and pipeline
@interface XProvideWirelet {}