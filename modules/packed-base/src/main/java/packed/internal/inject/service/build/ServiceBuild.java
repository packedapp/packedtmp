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
package packed.internal.inject.service.build;

import static java.util.Objects.requireNonNull;

import app.packed.base.AttributeMap;
import app.packed.base.Key;
import app.packed.config.ConfigSite;
import app.packed.inject.Provide;
import app.packed.inject.Service;
import packed.internal.inject.DependencyProvider;
import packed.internal.inject.service.ServiceBuildManager;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * Instances of this class are never exposed to end users. But instead wrapped.
 */
public abstract class ServiceBuild implements DependencyProvider, Service {

    /** The configuration site of this object. */
    private final ConfigSite configSite;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    private Key<?> key;

    /** The service manager that this service belongs to. */
    public ServiceBuildManager sm;

    public ServiceBuild(ConfigSite configSite, Key<?> key) {
        this.configSite = requireNonNull(configSite);
        this.key = requireNonNull(key);
    }

    public final void as(Key<?> key) {
        if (isFrozen) {
            throw new IllegalStateException("The key of the service can no longer be changed");
        }
        // requireConfigurable();
        // validateKey(key);
        // Det er sgu ikke lige til at validere det med generics signature....
        this.key = requireNonNull(key, "key is null");
    }

    private boolean isFrozen;

    public final void freeze() {
        isFrozen = true;
    }

    /**
     * Returns the configuration site of this configuration.
     * 
     * @return the configuration site of this configuration
     */
    public final ConfigSite configSite() {
        return configSite;
    }

    @Override
    public final Key<?> key() {
        return key;
    }

    @Override
    public abstract boolean isConstant();

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract RuntimeService newRuntimeNode(ServiceInstantiationContext context);

    public final Service toService() {
        if (isFrozen) {
            return this;
        }
        return new PackedService(key, configSite, isConstant());
    }

    public ServiceBuild rekeyAs(Key<?> key) {
        // NewKey must be compatible with type
        RekeyServiceBuild esb = new RekeyServiceBuild(this, key, configSite);
        return esb;
    }

    // cacher runtime noden...
    public final RuntimeService toRuntimeEntry(ServiceInstantiationContext context) {
        return context.transformers.computeIfAbsent(this, k -> {
            return k.newRuntimeNode(context);
        });
    }

    /** An implementation of {@link Service} because {@link ServiceBuild} is mutable. */
    private final class PackedService implements Service {

        /** The configuration site of the service. */
        private final ConfigSite configSite;

        /** The key of the service. */
        private final Key<?> key;

        /** Whether or not the service is a constant. */
        private final boolean isConstant;

        /**
         * Creates a new descriptor.
         * 
         * @param key
         *            the key of the service
         * @param configSite
         *            the config site of the service
         */
        private PackedService(Key<?> key, ConfigSite configSite, boolean isConstant) {
            this.key = requireNonNull(key);
            this.configSite = requireNonNull(configSite);
            this.isConstant = isConstant;
        }

        /** {@inheritDoc} */
        @Override
        public AttributeMap attributes() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return key;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ServiceDescriptor[key=" + key + ", configSite=" + configSite + "]";
        }

        /** {@inheritDoc} */
        @Override
        public boolean isConstant() {
            return isConstant;
        }
    }
}
