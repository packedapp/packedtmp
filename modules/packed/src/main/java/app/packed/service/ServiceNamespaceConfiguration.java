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
import java.util.Map;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.binding.Provider;
import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceConfiguration;
import app.packed.operation.OperationConfiguration;
import internal.app.packed.service.ServiceNamespaceHandle;
import internal.app.packed.service.PackedServiceLocator;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.MethodHandleUtil;

/**
 * A service namespace represents a namespace where every provided service in the service has a unique {@link Key key}.
 * And where multiple bindings may exist to each provided service.
 */
public final class ServiceNamespaceConfiguration extends NamespaceConfiguration<BaseExtension> {

    /**
     * Creates a new service namespace configuration.
     *
     * @param handle
     *            the namespace's handle
     * @param extension
     *            the base extension, which the service namespace belongs to
     *
     * @implNote invoked via
     *           {@link internal.app.packed.handlers.ServiceHandlers#newServiceNamespaceConfiguration(ServiceNamespaceHandle, BaseExtension)}
     */
    ServiceNamespaceConfiguration(ServiceNamespaceHandle handle, BaseExtension extension) {
        super(handle, extension);
    }

    // Hmm, specificere ved namespacet under provide?
    <T> OperationConfiguration provide(Class<T> key, Provider<? extends T> provider) {
        return provide(Key.of(key), provider);
    }

    <T> OperationConfiguration provide(Key<T> key, Provider<? extends T> provider) {
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
        Map<Key<?>, MethodHandle> result;
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

    /**
     * @param <T>
     *            the type of the provided service
     * @param key
     *            the key for which to provide the constant for
     * @param constant
     *            the constant to provide
     * @return a configuration representing the operation
     */
    <T> OperationConfiguration provideInstance(Class<T> key, T constant) {
        return provideInstance(Key.of(key), constant);
    }

    <T> OperationConfiguration provideInstance(Key<T> key, T constant) {
        // Nah skaber den forvirring? Nej det syntes det er rart
        // at have muligheden for ikke at scanne
        throw new UnsupportedOperationException();
    }

    // requires bliver automatisk anchoret...
    // anchorAllChildExports-> requireAllChildExports();
    public void requires(Class<?>... keys) {
        requires(Key.ofAll(keys));
    }

    /**
     * Explicitly adds the specified key to the list of required services. There are typically two situations in where
     * explicitly adding required services can be useful:
     * <p>
     * First, services that are cannot be specified at build time. But is needed later... Is mainly useful when we the
     * services to. For example, importAll() that injector might not a service itself. But other that make use of the
     * injector might.
     *
     *
     * <p>
     * Second, for manual service requirement, although it is often preferable to use contracts here
     * <p>
     * In any but the simplest of cases, contracts are useful
     *
     * @param keys
     *            the key(s) to add
     */
    public void requires(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsConfigurable();
        throw new UnsupportedOperationException();
    }

    // Think we need installPrototype (Which will fail if not provided or exported)
    // providePrototype would then be installPrototype().provide() // not ideal
    // Men taenker vi internt typisk arbejde op i mod implementering. Dog ikke altid
    // providePerRequest <-- every time the service is requested
    // Also these beans, can typically just be composites??? Nah

    public void requiresOptionally(Class<?>... keys) {
        requiresOptionally(Key.ofAll(keys));
    }

    /**
     * Adds the specified key to the list of optional services.
     * <p>
     * If a key is added optionally and the same key is later added as a normal (mandatory) requirement either explicitly
     * via # {@link #serviceRequire(Key...)} or implicitly via, for example, a constructor dependency. The key will be
     * removed from the list of optional services and only be listed as a required key.
     *
     * @param keys
     *            the key(s) to add
     */
    // How does this work with child services...
    // They will be consumed
    public void requiresOptionally(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsConfigurable();
        throw new UnsupportedOperationException();
    }
}
