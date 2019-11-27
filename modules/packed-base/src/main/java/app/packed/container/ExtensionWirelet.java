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

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

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
public abstract class ExtensionWirelet<T extends ExtensionWirelet.Pipeline<?, T, ?>> extends Wirelet {

    protected void extensionNotAvailable(Class<? extends Extension> extensionType) {
        // throw exception();
    }

    /**
     * Extension wirelet pipelines
     */
    // Kan only define one pipeline per extension... Maybe...
    // Kunne jo godt have @Nullable GreenPipeline, @Nullable BlackPipeline
    // Kan ogsaa flytte E til ExtensionWirelet, hvis vi ikke har #extension() paa pipelinen
    public static abstract class Pipeline<E extends Extension, P extends Pipeline<E, P, W>, W extends ExtensionWirelet<P>> implements Iterable<W> {

        /** Any previous pipeline. */
        Optional<P> previous;

        /** A list initially containing the wirelets that was used to create this pipeline. */
        List<W> wirelets;

        @Override
        public final void forEach(Consumer<? super W> action) {
            wirelets.forEach(action);
        }

        @Override
        public final Iterator<W> iterator() {
            return wirelets.iterator();
        }

        /** Invoked by the runtime immediately after the pipeline has been constructed. */
        protected void onInitialize() {}

        /**
         * If this pipeline was spawned from an existing pipeline, returns the pipeline, otherwise returns empty.
         * 
         * @return any pipeline this pipeline was spawned from
         */
        public final Optional<P> previous() {
            return previous;
        }

        //// Ideen er egentlig at vi f.eks. kan validere et deploy af et image med givne wirelets.
        // Saaledes at man kan vente med at instantiere den til man har brug for den...
        // protected void validate();

        // protected void optimize() <-- called by the runtime to optimize as much as possible

        @Override
        public final Spliterator<W> spliterator() {
            return wirelets.spliterator();
        }

        @Override
        public String toString() {
            return wirelets.toString();
        }

        // /**
        // * @param filter
        // * a predicate which returns {@code true} for wirelets to be removed
        // * @param action
        // * an action to be performed on each removed element
        // * @return whether or not any wirelets was removed
        // */
        // public boolean removeIf(Predicate<? super W> filter, Consumer<? super W> action) {
        // throw new UnsupportedOperationException();
        // }

        // /**
        // * Returns the extension this pipeline belongs to.
        // *
        // * @return the extension this pipeline belongs to
        // */
        // public final E extension() {
        // return extension;
        // }
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
