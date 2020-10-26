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
import packed.internal.inject.service.ServiceBuildManager;
import packed.internal.inject.service.WireletFromContext.ServiceWireletFrom;
import packed.internal.inject.service.WireletToContext;
import packed.internal.inject.service.WireletToContext.ServiceWireletTo;

/**
 * This class provide various wirelets that can be used to transform and filter services being pull and pushed into
 * containers.
 * 
 * Wirelets created by this class are processed in two separate occasions.
 * 
 * First pass wirelets are invoked at the wiring site
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

    /**
     * This method is similar to {@link #from(Consumer)} but it also provides the child's service contract.
     * <p>
     * The provided service contract is never effected by previous wirelet transformations.
     * 
     * @param transformer
     *            the transformer
     * @return the transforming wirelet
     */
    public static Wirelet from(BiConsumer<? super ServiceTransformer, ServiceContract> transformer) {
        requireNonNull(transformer, "transformer is null");
        return new ServiceWireletFrom() {
            @Override
            protected void process(ServiceBuildManager child) {
                child.exports().transform(transformer);
            }
        };
    }

    /**
     * Transforms the services
     * 
     * @param transformer
     *            the transformer
     * @return the transforming wirelet
     */
    public static Wirelet from(Consumer<? super ServiceTransformer> transformer) {
        requireNonNull(transformer, "transformer is null");
        return new ServiceWireletFrom() {
            /** {@inheritDoc} */
            @Override
            protected void process(ServiceBuildManager child) {
                child.exports().transform(transformer);
            }
        };
    }

    /**
     * Returns a wirelet that will invoke the specified action with the service contract of the cube that is being wired.
     * <p>
     * This wirelet is processed at the linkage site.
     * <p>
     * The contract being consumed is never effected by other wirelet transformations.
     * 
     * @param action
     *            the action to perform
     * @return a wirelet
     */
    public static Wirelet peekContract(Consumer<? super ServiceContract> action) {
        requireNonNull(action, "action is null");
        return from((t, c) -> action.accept(c));
    }

    public static <T> Wirelet provide(Class<T> key, T instance) {
        return provide(Key.of(key), instance);
    }

    public static <T> Wirelet provide(Key<T> key, T instance) {
        requireNonNull(key, "key is null");
        requireNonNull(instance, "instance is null");
        return to(t -> t.provideInstance(key, instance));
    }

    /**
     * Returns a wirelet that will provide the specified instance to the target cube. Iff the target cube has a service of
     * the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code to(t -> t.provideInstance(instance))}.
     * 
     * @param instance
     *            the service to provide
     * @return a wirelet that will provide the specified service
     * @see ServiceTransformer#provideInstance(Object)
     */
    public static Wirelet provide(Object instance) {
        requireNonNull(instance, "instance is null");
        return to(t -> t.provideInstance(instance));
    }

    public static Wirelet to(BiConsumer<? super ServiceTransformer, ServiceContract> transformer) {
        requireNonNull(transformer, "transformer is null");
        return new ServiceWireletTo() {
            @Override
            protected void process(WireletToContext context) {
                transformer.accept(context, context.childContract());
            }
        };
    }

    public static Wirelet to(Consumer<? super ServiceTransformer> transformer) {
        requireNonNull(transformer, "transformer is null");
        return new ServiceWireletTo() {
            @Override
            protected void process(WireletToContext context) {
                transformer.accept(context);
            }
        };
    }
}

/// 4 things we probably want to incorporate
// contracts
// transitive exports
// transitive requirements
// anchoring

// Preview... <-- filter annotations with preview????
// Fucking attributer jo... Service#Preview=true
// 

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
    // Altsaa de er jo lidt ligegyldige...
    // Kan bruge extensionen

    static Wirelet transitiveExportRequireAll() {
        throw new UnsupportedOperationException();
    }

    static Wirelet exportTransitiveAll() {
        return exportTransitiveIf(s -> true);
    }

    static Wirelet exportTransitiveIf(Predicate<? extends Service> filter) {
        throw new UnsupportedOperationException();
    }

    // Initial pass, final pass

    // But it will not anchor it...
    // Typically used for containers that aggregate child containers

    // exportTransitive
}

class TDropped {

    // Ideen er lidt at de ikke skal ind i containeren...
    static Wirelet exportTransitive(Class<?>... keys) {
        return exportTransitive(Key.of(keys));
    }

    static Wirelet exportTransitive(Key<?>... keys) {
        throw new UnsupportedOperationException();
    }

}