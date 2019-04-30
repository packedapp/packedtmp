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

import app.packed.extension.Extension;
import app.packed.util.Qualifier;
import packed.internal.inject.builder.InjectorBuilder;

/**
 *
 */

public final class InjectorExtension extends Extension<InjectorExtension> {

    final InjectorBuilder b;

    public InjectorExtension(InjectorBuilder b) {
        this.b = requireNonNull(b);
    }

    /**
     * Binds the specified instance as a new service.
     * <p>
     * The default key for the service will be {@code instance.getClass()}. If the type returned by
     * {@code instance.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will have the
     * qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param instance
     *            the instance to bind
     * @return a service configuration for the service
     */
    public <T> ServiceConfiguration<T> provide(T instance) {
        requireNonNull(instance, "instance");
        return b.provide(instance);
    }

    // ServicesDescriptor descriptor (extends Contract????) <- What we got so far....

    // Services are the default implementation of injection....

    // Export

    // Outer.. checker configurable, node. finish den sidste o.s.v.
    // Saa kalder vi addNode(inner.foo);

    // export

    // And then wrap it in ComponentServiceConfiguration....
    // void ServiceConfiguration<?> provide(ComponentConfiguration configuration);
}
