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
package sandbox.service;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.packed.container.Wirelet;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceIncomingTransformer;
import app.packed.service.ServiceOutgoingTransformer;
import app.packed.util.Key;

/**
 * This class provide wirelets that can be used to transform and filter services being pull and pushed into containers.
 *
 * Service wirelets are processed in two stages.
 *
 * In the first stage which happens immediately after the container receiving the service wirelet has been wired.
 *
 * The second stage is at the end of the configuration of the container
 *
 * Get some inspiration from streams
 *
 * <p>
 * All Wirelets on this class can only be used on container components. And the container must use a
 * {@link BaseExtension}.
 */
public final class ServiceWirelets {

    /** No instantiation. */
    private ServiceWirelets() {}

    // Hmmm, den virker jo fint paa top niveau...
    // Men nok daarlig midt i et trae.
    // Fordi der laver jo bare filtrering paa de services vi ikke
    // skal bruge

    public static Wirelet anchor(Class<?>... keys) {
        return anchor(Key.ofAll(keys));
    }

    public static Wirelet anchor(Key<?>... keys) {
        Set<Key<?>> ks = Set.of(keys);
        return anchorIf(s -> ks.contains(s));
    }

    /**
     * Anchors every accessible service exported by the child container into the parent container.
     * <p>
     * The wirelet can only be used when wiring non-root containers.
     *
     * @return a wirelet that will anchor all services
     * @see BaseExtension#anchorAll()
     */
    public static Wirelet anchorAll() {
        return anchorIf(t -> true);
    }

    // A service is accessible by a class or interface x. if the full key is Accessible

    public static Wirelet anchorIf(Predicate<? super Key<?>> filter) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet contractCheckCompatible(Consumer<? super ServiceContract> action) {
        requireNonNull(action, "action is null");
        throw new UnsupportedOperationException();
        // return transformIn(t -> action.accept(t.contract()));
    }

    /**
     * Returns a wirelet that will invoke the specified action with the service contract of the container that is being
     * wired. The wirelet it typically use to check the contents of.
     *
     * But it can also be used, for example, for debugging.
     *
     * <p>
     * This wirelet is processed at the linkage site.
     * <p>
     * The contract being provided is never effected previous transformations, for example, via {@link #crack(Consumer)}.
     *
     * @param action
     *            the action to perform
     * @return a wirelet
     */
    // was peekContract, but arguments were identical
    // verifyContract throws Verification exception
    // maybe even just verify... or validate
    public static Wirelet contractCheckExact(Consumer<? super ServiceContract> action) {
        requireNonNull(action, "action is null");
        // return transformIn(t -> action.accept(t.contract()));
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet provideInstance(Class<T> key, T instance) {
        return provideInstance(Key.of(key), instance);
    }

    /**
     * Returns a wirelet that will provide the specified instance to the target container. Iff the target container has a
     * service of the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code to(t -> t.provideInstance(instance))}.
     *
     * @param instance
     *            the service to provide
     * @return a wirelet that will provide the specified service
     */
    public static <T> Wirelet provideInstance(Key<T> key, T instance) {
        requireNonNull(key, "key is null");
        requireNonNull(instance, "instance is null");
        // Transform is not available at runtime
        return transformRequirements(t -> t.provideInstance(key, instance));
    }

//    public static Wirelet provideInstance(Object instance) {
//        requireNonNull(instance, "instance is null");
//        return transformTo(t -> t.provideInstance(instance));
//    }

    /**
     * This method is similar to {@link #crack(Consumer)} but also provides the service of the child.
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
    public static Wirelet transformExports(Consumer<? super ServiceOutgoingTransformer> transformation) {
        requireNonNull(transformation, "transformation is null");
        throw new UnsupportedOperationException();
    }

    public static Wirelet transformRequirements(Consumer<? super ServiceIncomingTransformer> transformation) {
        requireNonNull(transformation, "transformation is null");
        throw new UnsupportedOperationException();
//        return new Service2ndPassWirelet() {
//            @Override
//            protected void process(@Nullable InternalServiceExtension parent, InternalServiceExtension child, Map<Key<?>, ?> map) {
//                // child.ios.newServiceContract() to calc service contract
//                throw new UnsupportedOperationException();
//                //PackedServiceTransformer.transformInplace(map, transformation);
//            }
//        };
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

    static Wirelet exportTransitiveAll() {
        throw new UnsupportedOperationException();
    }

    //// Skal arbejde lidt paa det anchroring.
    //// og internerne services.
    // Altsaa de er jo lidt ligegyldige...
    // Kan bruge extensionen

    static Wirelet exportTransitiveIf(Predicate<?> filter) {
        throw new UnsupportedOperationException();
    }

    // Omvendt vil vi godt have en loesning til config. Da vi ikke vil
    // have elementer (som default) som vi ikke ved hvordan skal behandles
    static Wirelet ignoreUnrequiredServices() {
        throw new UnsupportedOperationException();
    }

    static Wirelet transitiveExportRequireAll() {
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
        return exportTransitive(Key.ofAll(keys));
    }

    static Wirelet exportTransitive(Key<?>... keys) {
        throw new UnsupportedOperationException();
    }

}