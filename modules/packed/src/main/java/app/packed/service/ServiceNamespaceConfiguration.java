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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceConfiguration;
import app.packed.namespace.NamespaceHandle;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.Provider;
import app.packed.util.Key;
import internal.app.packed.service.PackedServiceLocator;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.MethodHandleUtil;

/**
 *
 */
public abstract class ServiceNamespaceConfiguration extends NamespaceConfiguration<BaseExtension> {

    /**
     * @param handle
     */
    @SuppressWarnings("rawtypes")
    protected ServiceNamespaceConfiguration(NamespaceHandle handle) {
        super(handle);
    }

    // sbc.provide-> Knows the fucking service namespace...
    public <T> ServiceableBeanConfiguration<T> install(Class<T> implementation) {
        return extension().install(implementation);
    }

    <T> OperationConfiguration provide(Class<T> key, Provider<? extends T> provider) {
        return provide(Key.of(key), provider);
    }

    <T> OperationConfiguration provide(Key<T> key, Provider<? extends T> provider) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param <T>
     *            the type of the provided service
     * @param key
     *            the key for which to provide the constant for
     * @param constant
     *            the constant to provide
     * @return a configuration representing the operation
     */
    <T> OperationConfiguration provideConstant(Class<T> key, T constant) {
        return provideConstant(Key.of(key), constant);
    }

    <T> OperationConfiguration provideConstant(Key<T> key, T constant) {
        // Nah skaber den forvirring? Nej det syntes det er rart
        // at have muligheden for ikke at scanne
        throw new UnsupportedOperationException();
    }

    /**
     * Provides every service from the specified service locator.
     *
     * @param locator
     *            the service locator to provide services from
     * @throws KeyAlreadyInUseException
     *             if the service locator provides any keys that are already in use
     */
    // Map<Key<?>, ProvideServiceOperationConfiguraion>
    public Set<Key<?>> provideAll(ServiceLocator locator) {
        requireNonNull(locator, "locator is null");
        checkIsConfigurable();
        Map<Key<?>, MethodHandle> result = new HashMap<>();
        if (locator instanceof PackedServiceLocator psl) {
            result = CollectionUtil.copyOf(psl.entries(), e -> e.bindTo(psl.context()));
        } else {
            result = CollectionUtil.copyOf(locator.toProviderMap(), p -> MethodHandleUtil.PROVIDER_GET.bindTo(p));
        }
        // I think we will insert a functional bean that provides all the services

        // We can get the BaseExtension, but we are not in the same package
        // extension().container.sm.provideAll(result);
        return result.keySet(); // can probably return something more clever?
    }
}
