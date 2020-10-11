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
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import packed.internal.inject.service.WireletFromContext;
import packed.internal.inject.service.WireletFromContext.ServiceWireletFrom;

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

    public static Wirelet from(Consumer<? super ServiceTransformer> action) {
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet fromAddQualifier(Class<? extends Annotation> qualifier, Object value) {
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet fromMap(Class<T> from, Class<? super T> to) {
        // Skal vi checke????
        return fromMap(Key.of(from), Key.of(to));
    }

    public static <T> Wirelet fromMap(Key<T> from, Key<? super T> to) {
        // Changes the key of an entry (String -> @Left String
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet fromMapAll(Function<Service, ? super Key<?>> mapper) {
        fromMapAll(s -> s.key().withName("foo"));
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
     *            the action to perform
     * @return a peeking wirelet
     */
    public static Wirelet fromPeek(Consumer<? super ServiceRegistry> action) {
        requireNonNull(action, "action is null");
        return new ServiceWireletFrom() {
            /** {@inheritDoc} */
            @Override
            protected void process(WireletFromContext context) {
                context.peek(this, action);
            }
        };
    }

    public static Wirelet restrict(ServiceContract contract) {
        throw new UnsupportedOperationException();
    }
}

class ServiceWireletsSandbox {

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

    public static Wirelet disable() {
        // ServiceContract.EMPTY
        throw new UnsupportedOperationException();
    }

    public static Wirelet disableExports() {
        throw new UnsupportedOperationException();
    }

    public static Wirelet disableImports() {
        // Hmm hvorfor
        throw new UnsupportedOperationException();
    }

    public static Wirelet unconstanfy(Key<?> key) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet unconstanfyTo(Key<?> key) {
        throw new UnsupportedOperationException();
    }
}
