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

import app.packed.artifact.App;

/**
 * Extensions that define their own wirelets must extend this class.
 * 
 * Extension wirelets that uses the same extension pipeline type are processed in the order they are specified in. No
 * guarantees are made for extension wirelets that define for different extension pipeline types.
 * <p>
 * We need this class so we can see which extension the wirelet belongs to... Otherwise, we would not be able to tell
 * the user which extension was missing. When throwing an exception if the wirelet was specified, but the extension was
 * not used
 */
public abstract class ExtensionWirelet<T extends ExtensionWireletPipeline<?, T, ?>> extends Wirelet {

    /**
     * Invoked by the runtime whenever the user specified an extension wirelet for which a matching extension has not been
     * registered with the underlying container. For example, via {@link Bundle#link(Bundle, Wirelet...)} or
     * {@link App#execute(app.packed.artifact.Assembly, Wirelet...)}.
     * <p>
     * The default implementation throws an {@link IllegalArgumentException}.
     * 
     * @param extensionType
     *            the extension type that is missing
     */
    protected void extensionNotAvailable(Class<? extends Extension> extensionType) {
        throw new IllegalArgumentException(
                toString() + " can only be specified when the extension " + extensionType.getSimpleName() + " is used by the target container");
    }

}

// Grunden til vi gerne lave callback paa denne maade.
// Er at vi saa kan eksekvere dem i total order...
// Dvs f.eks. Wirelet.println("fooooBar").. Eller ting der skal saettes i andre extensions... f.eks.
// disableStackCapturing(), Service.provide(Stuff), enabledStackCapturing()...

// Vi bliver noedt til at lave noget sen-validering af en evt. parent extension

// Alternativt, skulle vi forbyde installering af extension, efter link()
// -> mere eller mindre alle metoder...

// Men nu har vi lige pludselig virale extensions...
// Det ville vi jo ikke kunne have...

// Ydermere kan en dependency, f.eks. vaere fra et andet bundle...
// Og dependency transformer vi endda.
// Som foerst bliver linket senere... Dvs vi kan ikke validere, foerend alle links er faerdige.
// Summasumarum vi maa validere til sidst.

// HVORFOR ikke bare en metode vi kan invoke fra extension'en?
// Det virker ikke naar vi image.with(some wirelets)....
// Fordi det kun er wirelets der bliver "koert".
// Vi koere ikke hver extension...
/// Maaske kan vi godt lave tmp bundles????
/// Hvis vi bare stopper inde graf hullumhej...
/// Det betyder dog ogsaa
