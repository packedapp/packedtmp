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
package app.packed.service.advanced;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import app.packed.context.Context;

/** Implementation of {@link ServiceResolution}. */
public record PackedServiceResolution(Class<? extends Context<?>>[] contexts, String[] namespaces, ServiceProviderKind[] order) implements ServiceResolver {

    @SuppressWarnings("unchecked")
    public PackedServiceResolution() {
        this(new Class[0], new String[] { "main" }, new ServiceProviderKind[] { ServiceProviderKind.OPERATION, ServiceProviderKind.BEAN,
                ServiceProviderKind.CONTEXT, ServiceProviderKind.NAMESPACE });
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Annotation> annotationType() {
        return ServiceResolver.class;
    }

    // Overriding accessors to return copies of the arrays
    @Override
    public Class<? extends Context<?>>[] contexts() {
        return Arrays.copyOf(contexts, contexts.length);
    }

    @Override
    public String[] namespaces() {
        return Arrays.copyOf(namespaces, namespaces.length);
    }

    @Override
    public ServiceProviderKind[] order() {
        return Arrays.copyOf(order, order.length);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ServiceResolver other && Arrays.equals(contexts, other.contexts()) && Arrays.equals(namespaces, other.namespaces())
                && Arrays.equals(order, other.order());
    }

    // Overriding hashCode to match equals (using deep hashing for arrays)
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[] { contexts, namespaces, order });
    }
}
