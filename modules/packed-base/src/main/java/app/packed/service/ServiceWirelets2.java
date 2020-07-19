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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;

/**
 * @apiNote In the future, if the Java language permits, {@link ServiceWirelets2} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface ServiceWirelets2 {

    /**
     * 
     * If the function returns a wirelet. The wirelet must have been created by one of the static methods of this class.
     * 
     * @param function
     *            the function
     * @return a wirelet
     */
    // computeWithDescriptorSetTo
    static Wirelet computeWithContract(Function<? super ServiceContract, @Nullable ? extends Wirelet> function) {
        throw new UnsupportedOperationException();
    }

    // many of these wirelets create synthetic components. For example to set the name
    // expose it as a service???
    // Should we take LocalService instead
//    static Wirelet computeWithSynth(Wirelet wirelet, Consumer<? extends ComponentConfiguration> configuration) {
//        throw new UnsupportedOperationException();// ideen er lidt computeWithSynth(provide(ffff), c.name("fofofo"));
//    }

    public static void main(String[] args) {
        provide("String", CharSequence.class);
        provide("String", new Key<CharSequence>() {});
        provide("String", s -> s.as(CharSequence.class));

        // computeWithContract(sc -> removeTo(sc.services().iterator().next()));
    }

    static Wirelet peekFrom(Consumer<? super ServiceDescriptorSet> action) {
        throw new UnsupportedOperationException();
    }

    static Wirelet peekTo(Consumer<? super ServiceDescriptorSet> action) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static Wirelet provide(Object instance) {
        requireNonNull(instance, "instance is null");
        return provide(instance, (Class) instance.getClass());
    }

    static <T> Wirelet provide(T instance, Class<? super T> key) {
        return provide(instance, Key.of(key));
    }

    static <T> Wirelet provide(T instance, Consumer<? extends ServiceComponentConfiguration<T>> configurator) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provide(T instance, Key<? super T> key) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideAll(Injector injector) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideFrom(Class<? super T> implementation, Consumer<? extends ServiceComponentConfiguration<T>> configurator) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideFrom(Class<? super T> implementation, Key<? super T> key) {
        throw new UnsupportedOperationException();
    }

    static Wirelet provideFrom(Class<?> implementation) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideFrom(Factory<? super T> implementation, Consumer<? extends ServiceComponentConfiguration<T>> configurator) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideFrom(Factory<? super T> implementation, Key<? super T> key) {
        throw new UnsupportedOperationException();
    }

    static Wirelet provideFrom(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <T> Wirelet provideTo(Class<?> implementation) {
        return provideTo(implementation, (Class) implementation);
    }

    static <T> Wirelet provideTo(Class<T> implementation, Class<? super T> key) {
        return provideTo(implementation, Key.of(key));
    }

    static <T> Wirelet provideTo(Class<T> implementation, Consumer<? extends ServiceComponentConfiguration<T>> configurator) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideTo(Class<T> implementation, Key<? super T> key) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideTo(Factory<? super T> implementation, Consumer<? extends ServiceComponentConfiguration<T>> configurator) {
        throw new UnsupportedOperationException();
    }

    static <T> Wirelet provideTo(Factory<? super T> implementation, Key<? super T> key) {
        throw new UnsupportedOperationException();
    }

    static Wirelet provideTo(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }
}
