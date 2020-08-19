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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attempts to find a wirelet of targets type.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
// Give me the wirelet...
//Assembly time must be true if used from sidecars...
//Parameters Y Fields
//TypeParameter->Injected into a function... nicee
//WireletLookup..
//@WireletFind

// Tror lidt vi er blevet enige om at droppe den...
// Maaske er den dynamisk... 
// Forstaaet paa den maade at alle wirelets som man kan se (visiable, readable)
// Er tilgaengelig...

// Ahhh for helvede optional......
// WireletReceive  
public @interface WireletReceive {

    Class<? extends Wirelet> type() default Wirelet.class;
    // Ideen var egentlig lidt at man kunne stjaele f.eks. en ServiceExtensionPipeline...
    // I constructeren...

    // Kunne ogsaa koere en consume paa en enkelt wirelet... Saa man ikke kan nedarve den...
    // boolean consumePipeline() default false;
}

// Som udgangspunkt skal de wirelets sgu consumes...
//@interface WSicar2 {
//    boolean requireConsumtion() default false;
//
//    boolean consumptionOptional() default false;
//}

// Why can this not be used on a Bundle?
// Because we do not have access to a bundle. 
// Until we have done bundle.lookup()

// wirelet.inherit = true + runtime -> bad combo???

// Sidecar injection at runtime...
