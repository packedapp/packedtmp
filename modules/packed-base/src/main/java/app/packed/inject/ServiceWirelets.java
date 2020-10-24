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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import packed.internal.inject.service.WireletFromContext;
import packed.internal.inject.service.WireletFromContext.ServiceWireletFrom;
import packed.internal.inject.service.wirelets.PackedDownstreamServiceWirelet;

/**
 * This class provide various wirelets that can be used to transform and filter services being pull and pushed into
 * containers.
 * 
 * The wirelets returned by methods on this class can be grouped into two groups.
 * 
 * First pass wirelets are invoked at the child linkage site
 * 
 * Second pass wirelets are invoked at the end of the configuration of the container
 * 
 * Get some inspiration from streams
 */
public final class ServiceWirelets {

    /** No instantiation. */
    private ServiceWirelets() {}

    public static Wirelet anchorAll() {
        throw new UnsupportedOperationException();
    }

    public static Wirelet from(Consumer<? super ServiceTransformer> transformer) {
        requireNonNull(transformer, "transformer is null");
        return new ServiceWireletFrom() {
            /** {@inheritDoc} */
            @Override
            protected void process(WireletFromContext context) {
                transformer.accept(context);
            }
        };
    }

    /**
     * Returns a wirelet that ddd when a container is linked.
     * <p>
     * This wirelet is processed at the linkage site.
     * 
     * @param action
     *            the action to perform
     * @return a wirelet
     */
    public static Wirelet peekContract(Consumer<? extends ServiceContract> action) {
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

    public static Wirelet to(BiConsumer<? super ServiceTransformer, ServiceContract> transformer) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet to(Consumer<? super ServiceTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    // restrict contract
    // provide <-- easy access
}

class XIdeasContract {

    public static Wirelet restrict(ServiceContract contract) {
        throw new UnsupportedOperationException();
    }
}

// A common pattern of x(class...), x(key...), xIf(Predicate), XAll()
class ServiceWSandbox {
    // anchor all unused services that are exported from a child
    static Wirelet anchor(Class<?>... keys) {
        throw new UnsupportedOperationException();
    }

    // When is this invoked???? First pass

    // Second pass

    static Wirelet anchor(Key<?>... keys) {
        throw new UnsupportedOperationException();
    }

    static Wirelet anchorAll() {
        throw new UnsupportedOperationException();
    }

    static Wirelet anchorIf(Predicate<? extends Service> filter) {
        throw new UnsupportedOperationException();
    }

    //// Skal arbejde lidt paa det anchroring.
    //// og internerne services.
    static Wirelet exportTransitive(Class<?>... keys) {
        return exportTransitive(Key.of(keys));
    }

    static Wirelet exportTransitive(Key<?>... keys) {
        throw new UnsupportedOperationException();
    }

    static Wirelet exportTransitiveAll() {
        return exportTransitiveIf(s -> true);
    }

    static Wirelet exportTransitiveIf(Predicate<? extends Service> filter) {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        anchor(String.class);
    }

    // Initial pass, final pass

    // But it will not anchor it...
    // Typically used for containers that aggregate child containers

    // exportTransitive

}