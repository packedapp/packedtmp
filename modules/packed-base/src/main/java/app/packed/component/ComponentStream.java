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
// components.hasMethodAnnotation(Provide.class, Inject.class).print();
public interface ComponentStream extends Stream<Component> {

    /**
     * Returns a new list containing all of the components in this stream in the order they where encountered. Is identical
     * to invoking {@code stream.collect(Collectors.toList())}.
     * <p>
     * This is a <em>terminal operation</em>.
     *
     * @return a new list containing all of the components in this stream
     */
    default List<Component> toList() {
        return collect(Collectors.toList());
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
    default ComponentStream distinct() {
        return this; // All components are distinct by default
    }

    /** {@inheritDoc} */
    @Override
    ComponentStream dropWhile(Predicate<? super Component> predicate);

    /** {@inheritDoc} */
    @Override
    ComponentStream filter(Predicate<? super Component> predicate);

    /** {@inheritDoc} */
    @Override
    ComponentStream limit(long maxSize);

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
    @Override // Only available from Java 9
    ComponentStream takeWhile(Predicate<? super Component> predicate);
}

//
// default <T> ComponentStream filterOnInstance(Class<T> type, Predicate<T> predicate) {
// throw new UnsupportedOperationException();
// }

// /**
// * Invokes the specified consumer for each component whose component instance is of the specific type.
// * <p>
// * Components that have not yet been fully initialized and where the initializing thread is different from the thread
// * calling this method are ignored.
// *
// * This is a <em>terminal operation</em>.
// *
// * @param <T>
// * the type of component instances to consume
// * @param instanceType
// * the type of instances to invoke the consumer for
// * @param consumer
// * the consumer to invoke for each match
// */
// <T> void forEachInstanceOf(Class<T> instanceType, BiConsumer<? super Component, ? super T> consumer);
//
// <T> void forEachInstanceOf(Class<T> instanceType, Consumer<? super T> consumer);
//
// /**
// * Returns a new stream of all component instances.
// *
// * @return a new stream of all component instances
// */
// // Tager kun dem der er instantiated... Maaske skal vi endda have et generalt isInstantiatedFilter? return map(e ->
// // e.getInstance());
// Stream<Object> instances();
//
// /**
// * Returns a new stream of all component instances that are of the specified type.
// * <p>
// * Invoking this method is equivalent to {@code instances().filter(e -> instanceType.isAssignableFrom(e.getClass()))}.
// *
// * @param <T>
// * The type of instances to include in the new stream
// * @param instanceType
// * the component instance types to include in the new stream
// * @return the new stream
// */
// @SuppressWarnings("unchecked")
// default <T> Stream<T> instancesOfType(Class<T> instanceType) {
// requireNonNull(instanceType, "instanceType is null");
// return (Stream<T>) instances().filter(e -> instanceType.isAssignableFrom(e.getClass()));
// }
