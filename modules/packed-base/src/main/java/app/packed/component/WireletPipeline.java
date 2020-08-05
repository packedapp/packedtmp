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

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.packed.base.Nullable;
import packed.internal.container.WireletPipelineContext;

/**
 * Extension wirelet pipelines
 * 
 * <p>
 * 
 */
//UseExtension -> Extension can be injected into the constructor of the pipeline
//The pipeline and the extension must be located in the same module as returned by 
//Constructor must be readable/open
//API Design note: Pipelines should in general not be accessible by end-users.

// If a pipeline implementation makes use of an extension. The extension and the pipeline implementation must be located in the same module as returned by
// ExtensionImplementation#getModule and WireletPipelineImplementation#getModule
public abstract class WireletPipeline<P extends WireletPipeline<P, W>, W extends Wirelet> implements Iterable<W>, Wirelet {

    /** The pipeline context, initialized immediately after the constructor of the pipeline has finished. */
    @Nullable
    WireletPipelineContext context;

    /**
     * Returns all wirelets in this pipeline.
     * 
     * @return a list of the wirelets in the pipeline
     * @throws IllegalStateException
     *             if called from the constructor of the pipeline
     */
    private WireletPipelineContext context() {
        WireletPipelineContext c = context;
        if (c == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of the pipeline, override #verify() instead.");
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public final void forEach(Consumer<? super W> action) {
        context().forEach(action);
    }

    public final void forEachLocal(Consumer<? super W> action) {
        // forEachCurrent
        // localalized
        context().forEach(action);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final Iterator<W> iterator() {
        return (Iterator<W>) context().toList().iterator();
    }

    /**
     * If this pipeline was spawned from another pipeline, returns the pipeline, otherwise returns empty.
     * 
     * @return any pipeline this pipeline was spawned from
     * @throws IllegalStateException
     *             if called from the constructor of the pipeline
     */
    @SuppressWarnings("unchecked")
    public final Optional<P> previous() {
        return Optional.ofNullable((P) context().previous());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final Spliterator<W> spliterator() {
        return (Spliterator<W>) context().toList().spliterator();
    }

    /**
     * Returns a stream of all wirelet in the pipeline.
     * 
     * @return a stream of all wirelet in the pipeline
     */
    public final Stream<W> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return context().toList().toString();
    }

    public final boolean isExpanding() {
        return false;
    }

    void initialize(WireletPipelineContext context) {
        this.context = context;
        verify();
    }

    /** Invoked by the runtime immediately after the pipeline has been constructed. */
    protected void verify() {} // expand

    static <T extends WireletPipeline<?, ?>> T ini(T pipeline, Wirelet... wirelets) {
        // Ideen er let at brugere kan bruge den for at faa populeret en pipeline???
        // Altsaa hvad hvis man two pipelines... Maaske man skal kunne angive flere pipelines
        throw new UnsupportedOperationException();
    }
}
// onImageGen() <- optimize stuff..

//// Ideen er egentlig at vi f.eks. kan validere et deploy af et image med givne wirelets.
// Saaledes at man kan vente med at instantiere den til man har brug for den...
// protected void validate();

// protected void optimize() <-- called by the runtime to optimize as much as possible

//Alternativ skal den have en annotation der peger paa pipelinen....

//Grunden til PipelineWirelet + WireletPipeline
//Er vi gerne vil have en let maade lige at smide noget ind i en container...
//Maaske kan man ogsaa bare tage en Optional<SomeWirelet>
//Disse kommer selvfoelgelig ikke med i optional services...
//Og de kan jo f.eks. foerst resolves paa runtime...

//Wirelets uden pipeline... Overskriv eller fejl hvis vi har mere end 2????
//Taenker bare overskriv. Latest vinder... Vi gennem det bare i et lazy initializeret map 
//med typen som key... Dvs. man skal angive exact type naar man vil have den injected...

//Der er ingen validering foerend paa runtime...

//Okay problemet med alm wirelets

//Conf vs Wirelet...

//OnWirelet unused (not part of a pipeline, not consumed anywhere...)
//Kan only define one pipeline per extension... Maybe...
//Kunne jo godt have @Nullable GreenPipeline, @Nullable BlackPipeline
//Kan ogsaa flytte E til ExtensionWirelet, hvis vi ikke har #extension() paa pipelinen

//WireletPipeline istedet for????? Nej vi har behov for E til at vide hvor vi skal smide den hen...

//
// public final void forEachLatest(Consumer<? super W> action) {
// wirelets.forEach(action);
// }

// Fra PipelineWirelet
/**
 * Extensions that define their own wirelets must extend this class.
 * 
 * Extension wirelets that uses the same extension pipeline type are processed in the order they are specified in. No
 * guarantees are made for extension wirelets that define for different extension pipeline types.
 * <p>
 * We need this class so we can see which extension the wirelet belongs to... Otherwise, we would not be able to tell
 * the user which extension was missing. When throwing an exception if the wirelet was specified, but the extension was
 * not used
 * 
 * @see WireletPipeline
 */
//
///**
// * Invoked by the runtime whenever the user specified an extension wirelet for which a matching extension has not been
// * registered with the underlying container. For example, via {@link Bundle#link(Bundle, Wirelet...)} or
// * {@link App#execute(app.packed.artifact.Assembly, Wirelet...)}.
// * <p>
// * The default implementation throws an {@link IllegalArgumentException}.
// * 
// * @param extensionType
// *            the extension type that is missing
// */
//// Maaske skal den vaere paa pipelinen???
//protected void extensionNotAvailable(Class<? extends Extension> extensionType) {
//    throw new IllegalArgumentException(
//            toString() + " can only be specified when the extension " + extensionType.getSimpleName() + " is used by the target container");
//}
// Grunden til vi gerne lave callback paa denne maade.
// Er at vi saa kan eksekvere dem i total order...
// Dvs f.eks. Wirelet.println("fooooBar").. Eller ting der skal saettes i andre extensions... f.eks.
// disableStackCapturing(), Service.provide(Stuff), enabledStackCapturing()...

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
