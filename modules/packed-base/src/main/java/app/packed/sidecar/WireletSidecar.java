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

import app.packed.container.PipelineWirelet;
import app.packed.container.Wirelet;
import app.packed.container.WireletPipeline;

/**
 * An annotation that can be used on subclasses of {@link Wirelet}. Classes that extend {@link Wirelet} are implicit
 * sidecars even without the use of this annotation. However, if the wirelet is part of a pipeline this must be
 * indicated by using this annotation.
 */
// Wirelet skal laves til et interface..
// Vi slipper af med PipelineWirelet
// Det er lettere at override den for subclasses...
// F.eks. MainArgs implements Wirelet... Men altsaa hvis vi kan injecte den... via @ProvideWirelet
// Behoever vi saa service???? IDontThinkSo...
// @ProvideWirelet kan ogsaa f.eks. go deeper... f.eks. ind i andre containere i wireletten

// Altsaa for Conf giver ret god mening... Vi siger vi skal have en Conf...

// Altsaa public klasser boer nok provide as service...

//@Inherited???
@interface WireletSidecar {

    // I think a boolean is fine. Can't imaging you would want to expose it as any other type
    boolean provideAsService() default false;

    Class<? extends WireletPipeline<?, ?>> pipeline() default NoWireletPipeline.class;

    // boolean requireAssemblyTime() must be used on assembly time
    // Cannot be used on an image after it has been created

    // assembleOnly
    // linkOnly
    // hostOnly
}

// Must use Optional/Nullable for wirelet
// Works for both wirelets and pipeline
@interface ProvideWirelet {}

class NoWireletPipeline extends WireletPipeline<NoWireletPipeline, NoWirelet> {}

class NoWirelet extends PipelineWirelet<NoWireletPipeline> {}

// vil automatisk bliver provided som service
@WireletSidecar(provideAsService = true)
class Doofar extends Wirelet {

}