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

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import packed.internal.inject.service.Service1stPassWirelet;
import packed.internal.inject.service.Service2ndPassWirelet;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.inject.service.build.BuildtimeService;
import packed.internal.inject.service.build.PackedServiceComposer;

/**
 * This class provide various wirelets that can be used to transform and filter services being pull and pushed into
 * containers.
 * 
 * Service wirelets are processed in two stages.
 * 
 * In the first stage which happens immediately after the container receiving the service wirelet has been wired.
 * 
 * The second stage is at the end of the configuration of the container
 * 
 * Get some inspiration from streams
 */
public final class ServiceWirelets {

    /** No instantiation. */
    private ServiceWirelets() {}

    public static Wirelet anchorAll() {
        return anchorIf(t -> true);
    }

    public static Wirelet anchor(Class<?> key) {
        return anchor(Key.of(key));
    }

    public static Wirelet anchor(Key<?> key) {
        return anchorIf(s -> s.key().equals(key));
    }

    public static Wirelet anchorIf(Predicate<? super Service> filter) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method is similar to {@link #from(Consumer)} but also provides the service of the child.
     * <p>
     * This wirelet is processed in the 1st stage, immediately after a container has been linked.
     * 
     * <p>
     * This is a <a href="package-summary.html#StreamOps">eager wirelet</a>.
     * <p>
     * The service contract provided to the consumer is never effected by previous wirelet transformations.
     * 
     * @param transformation
     *            the transformation to perform
     * @return the transforming wirelet
     */
    public static Wirelet from(BiConsumer<? super ServiceComposer, ServiceContract> transformation) {
        requireNonNull(transformation, "transformation is null");
        return new Service1stPassWirelet() {
            @Override
            protected void process(ServiceManagerSetup child) {
                child.exports().transform(transformation);
            }
        };
    }

    /**
     * Transforms the services
     * 
     * @param transformation
     *            the transformation to perform
     * @return the transforming wirelet
     */
    public static Wirelet from(Consumer<? super ServiceComposer> transformation) {
        requireNonNull(transformation, "transformation is null");
        return new Service1stPassWirelet() {
            /** {@inheritDoc} */
            @Override
            protected void process(ServiceManagerSetup child) {
                child.exports().transform(transformation);
            }
        };
    }

    /**
     * Returns a wirelet that will invoke the specified action with the service contract of the container that is being wired.
     * The wirelet it typically use to check the contents of.
     * 
     * But it can also be used, for example, for debugging.
     * 
     * <p>
     * This wirelet is processed at the linkage site.
     * <p>
     * The contract being provided is never effected previous transformations, for example, via {@link #from(Consumer)}.
     * 
     * @param action
     *            the action to perform
     * @return a wirelet
     */
    // was peekContract, but arguments were identical
    // verifyContract throws Verification exception
    // maybe even just verify... or validate
    public static Wirelet checkContract(Consumer<? super ServiceContract> action) {
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
     * Returns a wirelet that will provide the specified instance to the target container. Iff the target container has a service of
     * the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code to(t -> t.provideInstance(instance))}.
     * 
     * @param instance
     *            the service to provide
     * @return a wirelet that will provide the specified service
     * @see ServiceComposer#provideInstance(Object)
     */
    public static Wirelet provide(Object instance) {
        requireNonNull(instance, "instance is null");
        return to(t -> t.provideInstance(instance));
    }

    public static Wirelet to(BiConsumer<? super ServiceComposer, ServiceContract> transformation) {
        requireNonNull(transformation, "transformation is null");
        return new Service2ndPassWirelet() {
            @Override
            protected void process(@Nullable ServiceManagerSetup parent, ServiceManagerSetup child, Map<Key<?>, BuildtimeService> map) {
                PackedServiceComposer.transformInplaceAttachment(map, transformation, child.newServiceContract());
            }
        };
    }

    public static Wirelet to(Consumer<? super ServiceComposer> transformation) {
        requireNonNull(transformation, "transformation is null");
        return new Service2ndPassWirelet() {
            @Override
            protected void process(@Nullable ServiceManagerSetup parent, ServiceManagerSetup child, Map<Key<?>, BuildtimeService> map) {
                PackedServiceComposer.transformInplace(map, transformation);
            }
        };
    }
}

/// 4 things we probably want to incorporate
// contracts
// transitive exports
// transitive requirements
// anchoring

// Preview... <-- filter annotations with prΩeview????
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

    // Maybe check...
    // If validateXXX should always Validation
    static Wirelet validateExactRequirements() {
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