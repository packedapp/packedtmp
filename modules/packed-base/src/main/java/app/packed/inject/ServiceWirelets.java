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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import packed.internal.inject.service.wirelets.PackedDownstreamServiceWirelet;

/**
 * This class provide various wirelets that can be used to transform and filter services being pull and pushed into
 * containers.
 */

// provide -> Never removes, Never uses dependencies
// map -> removes existing
// insert -> insert new service possible with transformation
// remove -> removes by key
// peek
// compute -> [I think we replace it with ServiceTransformer... looks so much better]

// ---- Others
// bind <-- som default bliver kun services der bliver consumet somewhere binded naar man linker
// ------- Bind kunne force de her ting...

// contractUse, contractForce

// It is illegal to have multiple services with the same key at any part
// of the pipeline.
public final class ServiceWirelets {

    /** No instantiation. */
    private ServiceWirelets() {}

    public static Wirelet restrict(ServiceContract contract) {
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet addQualifierFrom(Class<? extends Annotation> qualifier, Object value) {
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet map(Class<T> from, Class<? super T> to) {
        return map(Key.of(from), Key.of(to));
    }

    public static <T> Wirelet map(Key<T> from, Key<? super T> to) {
        // Changes the key of an entry (String -> @Left String
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet mapAll(Function<Service, ? super Key<?>> mapper) {
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet mapAllFrom(Function<Service, ? super Key<?>> mapper) {
        mapAll(s -> s.key().withNameQualifier("foo"));
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet mapFrom(Class<T> from, Class<? super T> to) {
        return mapFrom(Key.of(from), Key.of(to));
    }

    public static <T> Wirelet mapFrom(Key<T> from, Key<? super T> to) {
        // Changes the key of an entry (String -> @Left String
        throw new UnsupportedOperationException();
    }

    public static Wirelet peek(Consumer<? super ServiceRegistry> action) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method exists mainly to support debugging, where you want to see which services are available at a particular
     * place in a the pipeline: <pre>
     * {@code 
     * Injector injector = some injector to import;
     *
     * Injector.of(c -> {
     *   c.importAll(injector, DownstreamServiceWirelets.peek(e -> System.out.println("Importing service " + e.getKey())));
     * });}
     * </pre>
     * <p>
     * This method is typically TODO before after import events
     * 
     * @param action
     *            the action to perform for each service descriptor
     * @return a peeking wirelet
     */
    public static Wirelet peekFrom(Consumer<? super ServiceRegistry> action) {
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet provide(Class<T> key, T instance) {
        return provide(Key.of(key), instance);
    }

    public static <T> Wirelet provide(Key<T> key, T instance) {
        return new PackedDownstreamServiceWirelet.ProvideInstance(key, instance);
    }

    /**
     * Returns a wirelet that will provide the specified service to the target container. Iff the target container has a
     * service of the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code provide(service.getClass(), service)}.
     * 
     * @param instance
     *            the service to provide
     * @return a wirelet that will provide the specified service
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Wirelet provide(Object instance) {
        requireNonNull(instance, "instance is null");
        return provide((Class) instance.getClass(), instance);
    }
}

class ServiceWireletsSandbox {

    public static Wirelet disable() {
        // ServiceContract.EMPTY
        throw new UnsupportedOperationException();
    }

    public static Wirelet disableImports() {
        // Hmm hvorfor
        throw new UnsupportedOperationException();
    }

    public static Wirelet disableExports() {
        throw new UnsupportedOperationException();
    }

    // Service transformere er saa meget lettere....
    // at bruge...
    // Det store problem er incoming hvor hvis vi laver bulk operations.
    // Saa bliver vi noedt til at tracke alle
    public static Wirelet compute(Function<? super ServiceRegistry, ? extends Optional<? extends Wirelet>> function) {
        // Must only provide ServiceWirelets...
        compute(e -> {
            if (e.isPresent(String.class)) {
                return Optional.of(ServiceWirelets.map(String.class, CharSequence.class));
            }
            return Optional.empty();
        });
        throw new UnsupportedOperationException();
    }

    public static Wirelet compute(Predicate<? super ServiceRegistry> filter, Function<? super ServiceRegistry, Wirelet> function) {
        // Must only provide ServiceWirelets...
        compute(f -> f.isPresent(String.class), e -> ServiceWirelets.map(String.class, CharSequence.class));
        throw new UnsupportedOperationException();
    }

    public static Wirelet computeFrom(Function<? super ServiceRegistry, ? extends Optional<? extends Wirelet>> function) {
        // Must only provide ServiceWirelets...
        compute(e -> {
            if (e.isPresent(String.class)) {
                return Optional.of(ServiceWirelets.map(String.class, CharSequence.class));
            }
            return Optional.empty();
        });
        throw new UnsupportedOperationException();
    }

    public static Wirelet computeFrom(Predicate<? super ServiceRegistry> filter, Function<? super ServiceRegistry, Wirelet> function) {
        // Must only provide ServiceWirelets...
        compute(f -> f.isPresent(String.class), e -> ServiceWirelets.map(String.class, CharSequence.class));
        throw new UnsupportedOperationException();
    }

    // Ideen er at vi kan aendre om ting er constants...
    // F.eks. hvis vi gerne vil cache noget??
    // Maaske have en map(dddd, boolean isConstant) istedet for
    // Er ikke super vild med dem...
    public static Wirelet constanfy(Key<?> key) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet constanfyTo(Key<?> key) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet unconstanfy(Key<?> key) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet unconstanfyTo(Key<?> key) {
        throw new UnsupportedOperationException();
    }
}