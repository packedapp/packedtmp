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
package app.packed.service.bridge;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.BaseExtensionWirelet;

/**
 * Various wirelets that applies to {@link BaseExtension}.
 */
public final class ServiceWirelets {
    private ServiceWirelets() {}

    // Double Provide overrides, Double Provide fails, I think just override

    // Can only be used together with a service namespace root
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
        return new ServiceWirelet(key);
    }

    public <T extends Record> Wirelet replaceAllIncoming(Class<T> recordType) {
        throw new UnsupportedOperationException();
    }

    public <T extends Record> Wirelet replaceAllIncoming(Key<? extends T> key) {
        throw new UnsupportedOperationException();
    }

    // The new container will always have a new main service namespace
    // Even If it didn't start it itself
    /**
     * {@return a wirelet that will force the targeting container to start a new main service namespace}
     * <p>
     * A child container may also choose to start its own service namespace.
     *
     */
    // requireExports();
    // encapsulatThis();

    // services().startNew();

    public static Wirelet newServiceNamespace() {
        return NewServiceNamespaceWirelet.INSTANCE;
    }

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
    public static Wirelet of(Consumer<? super ServiceNamespaceBridge> bridge) {
        requireNonNull(bridge, "bridge is null");
        throw new UnsupportedOperationException();
    }

    static class NewServiceNamespaceWirelet extends BaseExtensionWirelet {
        static final NewServiceNamespaceWirelet INSTANCE = new NewServiceNamespaceWirelet();

        /** {@inheritDoc} */
        @Override
        public void onBuild(ContainerSetup container) {
            container.wireletSpecs.newServiceNamespace = true;
        }

        /** {@inheritDoc} */
        @Override
        public void onUsed(BaseExtension extension) {}
    }

    static class ServiceWirelet extends BaseExtensionWirelet {
        private final Key<?> key;

        public ServiceWirelet(Key<?> key) {
            this.key = key;
        }

        /** {@inheritDoc} */
        @Override
        public void onBuild(ContainerSetup container) {
            IO.println("Provide key " + key);
        }

        /** {@inheritDoc} */
        @Override
        public void onUsed(BaseExtension extension) {}
    }
}
