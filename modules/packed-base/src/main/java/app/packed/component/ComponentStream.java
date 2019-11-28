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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactImage;
import app.packed.component.feature.AFeature;
import app.packed.container.Extension;
import packed.internal.component.PackedComponentStreamOption;

/**
 * A specialization of the {@link Stream} interface that deals with streams of {@link Component components}. An instance
 * of this class is normally acquired by invoking containerComponents or componentStream.
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

public interface ComponentStream extends Stream<Component> {

    /** {@inheritDoc} */
    @Override
    default ComponentStream distinct() {
        return this; // All components are distinct by default
    }

    /** {@inheritDoc} */
    @Override
    ComponentStream dropWhile(Predicate<? super Component> predicate);

    default <A> Stream<A> feature(Class<A> faetures) {
        throw new UnsupportedOperationException();
    }

    default <T extends AFeature<?, ?>> Stream<T> feature(T feature) {
        throw new UnsupportedOperationException();
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

    /** {@inheritDoc} */
    @Override
    ComponentStream filter(Predicate<? super Component> predicate);

    default ComponentStream inTopArtifact() {
        // Maybe
        return this;
    }
    // Component source();
    // int sourceDepth(); <- so we for example, can get all children....
    // alternative is filterOnDepth(Path relativeTo, int depth)
    // stream.filterOnDepth(App.path(), 4);
    // vs
    // stream.filterOnRelativeDepth(4);

    default ComponentStream inTopContainer() {
        // Hvad hvis vi er filtreret?????
        return this;
    }

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

    /********** Overridden to provide ComponentStream as a return value. **********/

    /** {@inheritDoc} */
    @Override
    ComponentStream limit(long maxSize);

    default ComponentStream onPath(String regexp) {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    ComponentStream peek(Consumer<? super Component> action);

    /** {@inheritDoc} */
    @Override
    ComponentStream skip(long n);

    /** Returns a new component stream where components are sorted by their {@link Component#path()}. */
    @Override
    default ComponentStream sorted() {
        return sorted((a, b) -> a.path().compareTo(b.path()));
    }

    /** {@inheritDoc} */
    @Override
    ComponentStream sorted(Comparator<? super Component> comparator);

    /** {@inheritDoc} */
    @Override
    ComponentStream takeWhile(Predicate<? super Component> predicate);

    /**
     * Returns a new list containing all of the components in this stream in the order they where encountered. Invoking this
     * method is identical to invoking {@code stream.collect(Collectors.toList())}.
     * <p>
     * This is a <em>terminal operation</em>.
     *
     * @return a new list containing all of the components in this stream
     */
    default List<Component> toList() {
        return collect(Collectors.toList());
    }

    default ComponentStream withFeatures(Class<?>... faetures) {
        throw new UnsupportedOperationException();
    }

    /**
     * Skal kun indeholde ting vi ikke kan have i streamen.
     */
    /// Ideen er lidt at hvis vi har en masse virtuelle komponenter. Saa gider vi ikke have dem med i default viewet....
    /// Men eftersom vi kun kan goere en stream mindre... Altsaa med mindre vi laver nogle flatmap tricks.
    // a.la. components.mapToVirtual....

    /**
     * Various options that can be used when creating a component stream.
     * 
     * @see Component#stream(Option...)
     * @see App#stream(Option...)
     * @see ArtifactImage#stream(Option...)
     */
    public interface Option {

        /**
         * Excludes the component from where the stream originated. However, any descendants of the component will still be
         * processed.
         * 
         * @return an option that excludes the component from which the stream is created
         */
        static ComponentStream.Option excludeOrigin() {
            return PackedComponentStreamOption.EXCLUDE_ORIGIN_OPTION;
        }

        /**
         * Include all components that is a part of an extension.
         * 
         * @return an option that includes all components that is a part of an extension
         */
        public static ComponentStream.Option includeExtensions() {
            return PackedComponentStreamOption.INCLUDE_EXTENSION_OPTION;
        }

        @SafeVarargs
        public static ComponentStream.Option includeExtensions(Class<? extends Extension>... extensionTypes) {
            throw new UnsupportedOperationException();
        }

        public static ComponentStream.Option inSameArtifact() {
            throw new UnsupportedOperationException();
        }

        /**
         * Only process components that are in the same container as the stream origin.
         * 
         * @return the option
         */
        public static ComponentStream.Option inSameContainer() {
            return PackedComponentStreamOption.IN_SAME_CONTAINER_OPTION;
        }

        // maxDepth, maxRelativeDepth
        // maxDepth, maxAbsoluteDepth <---- Taenker man oftest vil bruge relative, saa denne er nok bedst
        // Omvendt skal maxDepth jo helst passe til component.depth som jo er absolute....

        // processChildren, pro
    }
}
