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

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import app.packed.extension.Extension;
import packed.internal.component.PackedComponentStreamOption;

/**
 * A specialization of the {@link Stream} interface that deals with streams of {@link ComponentMirror components}. An
 * instance of this class is normally acquired by.
 *
 * <pre>
 * App app  = ...
 * System.out.println(&quot;Number of components = &quot; + app.components().count());
 * </pre>
 * <p>
 * This interface will be extended in the future with additional methods.
 */

// Cross
// withDepthLessThen();<-maaske bedre med en traverser, det skal jo vaere relativt i forhold til node.stream().
//// Maaske bedre at snakke om levels..

// withModulePrefix() <- Does not stop traversel
// print.... <- prints hierachels
// print(Function<Component->String> <-maintains _-- alle stregerne
// Maaske skal vi have en container printer.....
// ContainerPrinter.print (.... or TreePrinter....).. Ideen er lidt at
// sort(), skip, osv. ikke passer searlig godt ind...
// filterOn

// Altsaa vi kan vel bruge den her ogsaa paa build tid.....????
// Hvis det er ren information... Lifecycle kan vaere UNKNOWN...
// components().hasMethodAnnotation(Provide.class, Inject.class).print();

// Find all components that have a Dependency on a String
// Is this available at runtime????
// components().filter(ServiceFilters.hasDependency(String.class))).print();

// ComponentStream<T extends Component> extends Stream<T> ????
// Vi har mere eller mindre aldrig brug for at gemm
// Men der skal lige lidt mere guf paa Component sub interfaces...Foerend jeg gider lave det

// we should remove AttributedElementStream...
// But keep an abstract version internally

//  Drop Stream.. og have
// stream() metode istedet for...
// ModelWalker<Component

// Svaert ved at se den overlever...

// Altsaa Stream er sgu ikke et saerlig godt interface...
// distinct(), sorted, max, min... hvad skal vi bruge dem til
// skip giver ogsaa lidt mening..


// Ved ikke om 
// stream().members();

public interface ComponentMirrorStream extends Stream<ComponentMirror> {

//    /**
//     * Returns a stream that only contains containers.
//     * 
//     * @return a stream that only contains containers
//     */
//    default ComponentStream containers() {
//        return filterOnType(ComponentDescriptor.CONTAINER);
//    }

    /** {@inheritDoc} */
    @Override
    default ComponentMirrorStream distinct() {
        return this; // All components are distinct by default
    }

    // @SuppressWarnings("unchecked")
    // default <A> void forEachFeature(FeatureKey<A> feature, BiConsumer<Component, ? super A> action) {
    // // Dropper det <A, B, C> for streams er det jo info der giver mening
    // requireNonNull(feature, "feature is null");
    // requireNonNull(action, "action is null");
    // forEach(c -> {
    // Object o = c.features().get(feature);
    // if (o != null) {
    // action.accept(c, (A) o);
    // }
    // // if (o.isPresent()) {
    // // action.accept(c, o.get());
    // // }
    // });
    // }
//
//    default ComponentStream filterOnType(ComponentDescriptor type) {
//        requireNonNull(type, "type is null");
//        return filter(e -> e.model() == type);
//    }

    // /**
    // * Returns a component stream consisting of all components in this stream where the specified tag is present.
    // * <p>
    // * Usage:
    // *
    // * <pre>
    // * Container c;
    // * System.out.println("Number of important components : " + c.stream().withTag("IMPORTANT").count());
    // * </pre>
    // * <p>
    // * This is an <em>intermediate operation</em>.
    // *
    // * @param tag
    // * the tag that must be present
    // * @return the new stream
    // */
    // default ComponentStream withTag(String tag) {
    // requireNonNull(tag, "tag is null");
    // return filter(e -> e.tags().contains(tag));
    // }
    //
    // default <T> ComponentStream filterOnType(Class<T> type) {
    // return filter(c -> {
    // return c.instance().getClass().isAssignableFrom(type);
    // });
    // }

//    default ComponentStream onPath(String regexp) {
//        return this;
//    }

    // Er det components med sidecars der provider mindst en feature instans???
    // Forstaaet paa den maade at vi bliver noedt til at kalde en metode paa sidecaren..
    // Og se om der faktisk er nogen elementer i den liste der kommer tilbage...
    // Men ja igen... Det er jo ikke performance orienteret.
//    default ComponentStream withFeatures(@SuppressWarnings("unchecked") Class<? extends Feature>... featureTypes) {
//        throw new UnsupportedOperationException();
//    }

    /********** Overridden to provide ComponentStream as a return value. **********/

    /** {@inheritDoc} */
    @Override
    ComponentMirrorStream dropWhile(Predicate<? super ComponentMirror> predicate);

    /** {@inheritDoc} */
    @Override
    ComponentMirrorStream filter(Predicate<? super ComponentMirror> predicate);

//    default <T extends AFeature<?, ?>> Stream<T> feature(T feature) {
//        throw new UnsupportedOperationException();
//    }

    /** {@inheritDoc} */
    @Override
    ComponentMirrorStream limit(long maxSize);

    /** {@inheritDoc} */
    @Override
    ComponentMirrorStream peek(Consumer<? super ComponentMirror> action);

    /** {@inheritDoc} */
    @Override
    ComponentMirrorStream skip(long n);

    /** Returns a new component stream where components are sorted by their {@link ComponentMirror#path()}. */
    @Override
    default ComponentMirrorStream sorted() {
        return sorted((a, b) -> a.path().compareTo(b.path()));
    }

    /** {@inheritDoc} */
    @Override
    ComponentMirrorStream sorted(Comparator<? super ComponentMirror> comparator);

    /** {@inheritDoc} */
    @Override
    ComponentMirrorStream takeWhile(Predicate<? super ComponentMirror> predicate);

    /**
     * Skal kun indeholde ting vi ikke kan have i streamen.
     */
    /// Ideen er lidt at hvis vi har en masse virtuelle komponenter. Saa gider vi ikke have dem med i default viewet....
    /// Men eftersom vi kun kan goere en stream mindre... Altsaa med mindre vi laver nogle flatmap tricks.
    // a.la. components.mapToVirtual....

    // Maaske er en option -> Include X | Go Deeper | Order to return components

    // Det er vel de 3 ting alt er bygget op omkring...
    // in same container -> src.container = include + go deeper

    /**
     * Various options that can be used when creating component streams.
     * <p>
     * Options control
     * 
     * Whether or not a component should be included in the stream
     * 
     * Whether or not the children of a given component should be processed
     * 
     * The order in which children should be processed
     * 
     * @see ComponentMirror#stream(Option...)
     */
    // I virkeligheden er det system view options.
    // Noget af det vil jeg mene..
    public interface Option {
        // hideOrigin?
        // showExtensions
        // restrictSameContainer

        // depth, depthContainers, depthArtifacts.
        // The depth is always relative to the origin's depth

        // Vil vel ogsaa gerne bruge den paa component.traversel

        // FollowUnitialized guests...

        /**
         * Include components that belongs to an extension .
         * 
         * @return an option that includes all components that are part of an extension
         */
        public static ComponentMirrorStream.Option includeExtensions() {
            return PackedComponentStreamOption.INCLUDE_EXTENSION_OPTION;
        }

        /**
         * Include components that belongs to any of the specified extension types is non-empty and contained in the specified
         * varargs).
         * 
         * @param extensionType
         *            extension classes that should be included in the stream
         * @return an option that includes all components that belongs to any of the specified extension types
         */
        @SafeVarargs
        public static ComponentMirrorStream.Option includeExtensions(Class<? extends Extension>... extensionType) {
            throw new UnsupportedOperationException();
        }

        /**
         * Only process components that are in the same container as the stream origin.
         * <p>
         * If the origin is not in a container? Fail??? Empty Stream, Only Origin
         * 
         * @return an option that selects only the components that are in the same container as the stream origin.
         */
        public static ComponentMirrorStream.Option inOriginContainer() {
            return PackedComponentStreamOption.IN_ORIGIN_CONTAINER_OPTION;
        }

        /**
         * @param depth
         *            the maximum depth of any component relative to the origin's depth
         * @return an option
         * @throws IllegalArgumentException
         *             if the specified depth is negative
         */
        // maxRelativeDepth?
        public static ComponentMirrorStream.Option maxDepth(int depth) {
            return PackedComponentStreamOption.INCLUDE_EXTENSION_OPTION;
        }

        // Not sure we are ready for this...
        static ComponentMirrorStream.Option parallel() {
            throw new UnsupportedOperationException();
        }

        // Fail if not??
        public static ComponentMirrorStream.Option partOfSame(ComponentScope boundaryType) {
            return PackedComponentStreamOption.INCLUDE_EXTENSION_OPTION;
        }

        /**
         * Excludes the component from where the stream originated in the component stream. But process any of its descendants
         * normally.
         * 
         * @return an option that excludes the component from where the stream is originated
         */
        static ComponentMirrorStream.Option skipOrigin() {
            return PackedComponentStreamOption.EXCLUDE_ORIGIN_OPTION;
        }

        // maxDepth, maxRelativeDepth
        // maxDepth, maxAbsoluteDepth <---- Taenker man oftest vil bruge relative, saa denne er nok bedst
        // Omvendt skal maxDepth jo helst passe til component.depth som jo er absolute....

        // processChildren, pro
    }
}
// Component source();
// int sourceDepth(); <- so we for example, can get all children....
// alternative is filterOnDepth(Path relativeTo, int depth)
// stream.filterOnDepth(App.path(), 4);
// vs
// stream.filterOnRelativeDepth(4);
