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

import app.packed.base.Nullable;
import packed.internal.container.WireletPipelineModel;

/**
 * Extension wirelet pipelines
 * 
 * <p>
 * 
 * @see PipelineWirelet
 */
//UseExtension -> Extension can be injected into the constructor of the pipeline
//The pipeline and the extension must be located in the same module as returned by 
//Constructor must be readable/open
//API Design note: Pipelines should in general not be accessible by end-users.

// If a pipeline implementation makes use of an extension. The extension and the pipeline implementation must be located in the same module as returned by
// ExtensionImplementation#getModule and WireletPipelineImplementation#getModule
public abstract class WireletPipeline<E extends Extension, P extends WireletPipeline<E, P, W>, W extends PipelineWirelet<P>> implements Iterable<W> {

    /** Any previous pipeline, is initialized immediately after the constructor of the pipeline has finished. */
    @Nullable
    Optional<P> previous;

    /**
     * A list initially containing the wirelets that was used to create this pipeline, is initialized immediately after the
     * constructor of the pipeline has finished.
     */
    @Nullable
    List<W> wirelets;

    @SuppressWarnings("unchecked")
    protected void extensionNotAvailable() {
        Class<? extends Extension> extensionType = WireletPipelineModel.of((Class<? extends WireletPipeline<?, ?, ?>>) getClass()).extensionType;
        // Bliver noedt til at liste alle wirelets
        throw new IllegalArgumentException(
                toString() + " can only be specified when the extension " + extensionType.getSimpleName() + " is used by the target container");
    }

    /** {@inheritDoc} */
    @Override
    public final void forEach(Consumer<? super W> action) {
        wirelets().forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public final Iterator<W> iterator() {
        return wirelets().iterator();
    }

    /**
     * If this pipeline was spawned from an existing pipeline, returns the pipeline, otherwise returns empty.
     * 
     * @return any pipeline this pipeline was spawned from
     * @throws IllegalStateException
     *             if called from the constructor of the pipeline
     */
    public final Optional<P> previous() {
        Optional<P> p = previous;
        if (p == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of the pipeline, override #verify() instead.");
        }
        return p;
    }

    /** {@inheritDoc} */
    @Override
    public final Spliterator<W> spliterator() {
        return wirelets().spliterator();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return wirelets().toString();
    }

    /** Invoked by the runtime immediately after the pipeline has been constructed. */
    protected void verify() {}

    /**
     * Returns all wirelets in this pipeline.
     * 
     * @return a list of the wirelets in the pipeline
     * @throws IllegalStateException
     *             if called from the constructor of the pipeline
     */
    private List<W> wirelets() {
        List<W> w = wirelets;
        if (w == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of the pipeline, override #verify() instead.");
        }
        return w;
    }

    static Optional<Class<? extends Extension>> extensionTypeOf(Class<? extends Wirelet> wirelet) {
        // is PipelinedWirelet
        // isPipeline of type extension...
        //
        throw new UnsupportedOperationException();
    }

    static <T extends WireletPipeline<?, ?, ?>> T ini(T pipeline, Wirelet... wirelets) {
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

//Ideen er vel at bruge skal kunne laves deres egne pipelines.... som man saa kan faa injected....

//MyPipeline extends WireletPipeline...
//Altså skal man have Optional<Somepipeline> eller bare en empty Pipeline injected...

//Det betyder ogsaa at wirelets skal belonge til en pipeline saa Wirelet<P extends Pipeline>
//Men det er da irriterende for brugere...

//Wirelet<?> gider vi ikke have...

//PipelineWirelet<T extends>

//Dvs 5 klasser?
//Wirelet
//PipelineWirelet
//WireletPipeline

//ExtensionWirelet
//ExtensionWireletPipeline

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