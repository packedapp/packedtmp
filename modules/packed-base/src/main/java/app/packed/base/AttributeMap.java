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
package app.packed.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import packed.internal.base.attribute.EmptyAttributeMap;

/**
 *
 * <P>
 * The naming of the various methods of this interface has been adopted from {@link Optional}.
 */

// A key = Class + name

//AttributeMap???
// Syntes det bedre beskriver det en AttributeSet...

// Et attribute map mapper attributes til en vaerdi

// Kopiere noget mere fra Optional...

// Syntes navngivningen skal gaa igen i Config...

//attributes.ifPresent(ServiceExtension.CONTAINS, e->sysout("number of foo " + e);
public interface AttributeMap {

    default Set<Map.Entry<Attribute<?>, Object>> entrySet() {
        Map<Attribute<?>, Object> m = new HashMap<>();
        keys().forEach(k -> m.put(k, get(k)));
        return m.entrySet();
    }

    /**
     * If the specified attribute is present, returns the value of the attribute, otherwise throws
     * {@code NoSuchElementException}.
     * 
     * @param <A>
     *            the attribute value type
     * @param attribute
     *            the attribute
     * @return the non-{@code null} value of the specified attribute
     * @throws NoSuchElementException
     *             if the specified attribute is not present
     */
    // If the attribute has a default value this will be returned???
    <A> A get(Attribute<A> attribute);

    /**
     * If the specified attribute is present, performs the given action with its value, otherwise does nothing.
     * 
     * @param <A>
     *            the type of attribute value
     * @param attribute
     *            the attribute
     * @param action
     *            the action to be performed, if an attribute is present
     */
    default <A> void ifPresent(Attribute<A> attribute, Consumer<? super A> action) {
        if (isPresent(attribute)) {
            action.accept(get(attribute));
        }
    }

    /**
     * If the specified attribute is present, performs the given action with its value, otherwise performs the given
     * empty-based action.
     *
     * @param action
     *            the action to be performed, if an attribute is present
     * @param emptyAction
     *            the empty-based action to be performed, if the attribute is not present
     * @param <A>
     *            the attribute value type
     */
    default <A> void ifPresentOrElse(Attribute<A> attribute, Consumer<? super A> action, Runnable emptyAction) {
        if (isPresent(attribute)) {
            action.accept(get(attribute));
        } else {
            emptyAction.run();
        }
    }

    /**
     * Returns whether or not this map contains any entries.
     * 
     * @return whether or not this map contains any entries
     */
    default boolean isEmpty() {
        return keys().isEmpty();
    }

    boolean isPresent(Attribute<?> attribute);

    /**
     * Returns a set of all the attributes that are present in this map.
     * 
     * @return a set of all the attributes that are present in this map
     */
    Set<Attribute<?>> keys();

    /**
     * If a value is present, returns an {@code Optional} describing (as if by {@link Optional#ofNullable}) the result of
     * applying the given mapping function to the value, otherwise returns an empty {@code Optional}.
     * <p>
     * If the mapping function returns a {@code null} result then this method returns an empty {@code Optional}.
     *
     * @param mapper
     *            the mapping function to apply to a value, if present
     * @param <T>
     *            the attribute value type
     * @param <U>
     *            The type of the value returned from the mapping function
     * @return an {@code Optional} describing the result of applying a mapping function to the value of this
     *         {@code Optional}, if a value is present, otherwise an empty {@code Optional}
     */
    // Optional<String> s = get(ATR1).map(sdsd);
    // Optional<String> s = map(ATR1, sdsd);

    default <T, U> Optional<U> map(Attribute<T> attribute, Function<? super T, ? extends U> mapper) {
        throw new UnsupportedOperationException();
    }

    /**
     * If an attribute is present, returns its value, otherwise returns {@code other}.
     *
     * @param other
     *            the value to be returned, if the attribute is not present is present.
     * @param <T>
     *            the attribute value type
     * @return the attribute value, if present, otherwise {@code other}
     */
    @Nullable
    default <T> T orElse(Attribute<T> attribute, @Nullable T other) {
        if (isPresent(attribute)) {
            return get(attribute);
        }
        return other;
    }

    /**
     * If an attribute is present, returns its value, otherwise returns the result produced by the supplying function.
     *
     * @param supplier
     *            the supplying function that produces a value to be returned
     * @param <T>
     *            the attribute value type
     * @return the attribute value, if present, otherwise the result produced by the supplying function
     */
    @Nullable
    default <T> T orElseGet(Attribute<T> attribute, Supplier<? extends T> supplier) {
        throw new UnsupportedOperationException();
    }

    default <T, S> S orStuff(Attribute<T> attribute, Function<T, S> f, S defaultValue) {
        throw new UnsupportedOperationException();
    }

    default <T> boolean testIfPresent(Attribute<T> attribute, Predicate<? super T> predicate, boolean defaultValue) {
        throw new UnsupportedOperationException();
    }

    default <A extends Collection<E>, E> boolean valueContains(Attribute<A> attribute, E element) {
        Collection<E> col = get(attribute);
        return col.contains(element);
    }

    /**
     * Returns an unmodifiable map containing zero mappings.
     *
     * @return an empty {@code AtttributeMap}
     */
    static AttributeMap of() {
        return EmptyAttributeMap.INSTANCE;
    }
}

// AttributeSet open(Lookup lookup);
// Will show stuff that is hidden to....

// or privateLookup idk