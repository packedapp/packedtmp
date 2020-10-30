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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.Service;
import app.packed.inject.ServiceLocator;
import app.packed.inject.ServiceTransformation;
import packed.internal.inject.service.build.ConstantServiceBuild;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.inject.service.sandbox.RuntimeAdaptorServiceBuild;
import packed.internal.util.ForwardingMap;
import packed.internal.util.ForwardingStrategy;

/** Implementation of {@link ServiceTransformation}. */
// Currently used
// ServiceLocator.transform
// ServiceLocator.of
// Wirelet.toTransform
// Wirelet.fromTransform
// ServiceExtension.extensionTransform
public final class PackedServiceTransformer extends AbstractServiceRegistry implements ServiceTransformation {

    /** The services that we are transforming */
    // An alternative implementation would be to have a backing map and a filter
    // However then asMap() would be difficult to implement.
    public final Map<Key<?>, AbstractService> services;

    private Map<Key<?>, Service> asMap;

    @SuppressWarnings("unchecked")
    private PackedServiceTransformer(Map<Key<?>, ? extends AbstractService> services) {
        this.services = (Map<Key<?>, AbstractService>) requireNonNull(services);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<Key<?>, Service> asMap() {
        Map<Key<?>, Service> es;
        // TODO fix map for remove
        return (es = asMap) == null ? (asMap = new ForwardingMap<>((Map) services, new ForwardingStrategy())) : es;
    }

    private AbstractService checkKeyExists(Key<?> key) {
        AbstractService s = services.remove(key);
        if (s == null) {
            throw new NoSuchElementException("No service with the specified key exists, key = " + key);
        }
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction) {
        requireNonNull(key, "key is null");
        requireNonNull(decoratingFunction, "decoratingFunction is null");
        services.put(key, checkKeyExists(key).decorate(decoratingFunction));
    }

    @Override
    public <T> void provideInstance(Key<T> key, T instance) {
        requireNonNull(key, "key is null");
        requireNonNull(instance, "instance is null");
        // instance must be assignable to key raw type, maybe the build entry should check that...
        services.put(key, new ConstantServiceBuild(key, instance));
    }

    /** {@inheritDoc} */
    @Override
    public void rekey(Key<?> existingKey, Key<?> newKey) {
        requireNonNull(existingKey, "existingKey is null");
        requireNonNull(newKey, "newKey is null");
        if (existingKey.equals(newKey)) {
            throw new IllegalStateException("Cannot rekey the same key, key = " + existingKey);
        } else if (services.containsKey(newKey)) {
            throw new IllegalStateException("A service with newKey already exists, key = " + newKey);
        }
        services.put(newKey, checkKeyExists(existingKey).rekeyAs(newKey));
    }

    /** {@inheritDoc} */
    @Override
    public void rekeyAll(Function<Service, @Nullable Key<?>> function) {
        requireNonNull(function, "function is null");

        Map<Key<?>, AbstractService> newServices = new HashMap<>();
        for (AbstractService s : services.values()) {
            Key<?> key = function.apply(s);
            // If the function returns null we exclude the key
            if (key != null) {
                Key<?> existing = s.key();
                // we need to rekey the service if the function return a new key
                if (!key.equals(existing)) {
                    s = s.rekeyAs(existing);
                }
                // Make sure that we do not end up with multiple services with the same key
                if (newServices.putIfAbsent(key, s) != null) {
                    throw new IllegalStateException("A service with key already exists, key = " + key);
                }
            }
        }
        // We replace in-map just in case somebody got the idea of storing
        // ServiceRegistry#keys() somewhere
        services.clear();
        services.putAll(newServices);
    }

    public static void transformInplace(Map<Key<?>, ? extends AbstractService> services, Consumer<? super ServiceTransformation> transformer) {
//        PackedServiceTransformer pst = new PackedServiceTransformer(services);
//        public void transform(BiConsumer<? super ServiceTransformer, ServiceContract> transformer) {
//            if (resolvedExports == null) {
//                resolvedExports = new LinkedHashMap<>();
//            }
//            transformer.accept(null, sm.newServiceContract());
//        }

        PackedServiceTransformer dst = new PackedServiceTransformer(services);
        transformer.accept(dst);
    }

    public static <T> void transformInplaceAttachment(Map<Key<?>, ? extends AbstractService> services,
            BiConsumer<? super ServiceTransformation, ? super T> transformer, T attachment) {

        PackedServiceTransformer dst = new PackedServiceTransformer(services);
        transformer.accept(dst, attachment);
    }

    /**
     * Creates a new service locator.
     * 
     * @return the new service locator
     */
    private ServiceLocator toServiceLocator() {
        Map<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext();
        for (AbstractService e : services.values()) {
            runtimeEntries.put(e.key(), ((ServiceBuild) e).toRuntimeEntry(con));
        }
        return new PackedInjector(ConfigSite.UNKNOWN, runtimeEntries);
    }

    public static ServiceLocator transform(Consumer<? super ServiceTransformation> transformer, Collection<RuntimeService> services) {
        requireNonNull(transformer, "transformer is null");
        HashMap<Key<?>, ServiceBuild> m = new HashMap<>();
        for (RuntimeService s : services) {
            m.put(s.key(), new RuntimeAdaptorServiceBuild(ConfigSite.UNKNOWN, s));
        }
        PackedServiceTransformer dst = new PackedServiceTransformer(m);
        transformer.accept(dst);
        return dst.toServiceLocator();
    }

    /**
     * Creates a new service locator from scratch by using the specified transformer.
     * 
     * @param transformer
     *            the transformer used to create the locator
     * @return a new service locator
     */
    public static ServiceLocator of(Consumer<? super ServiceTransformation> transformer) {
        requireNonNull(transformer, "transformer is null");
        PackedServiceTransformer psm = new PackedServiceTransformer(new HashMap<>());
        transformer.accept(psm);
        return psm.toServiceLocator();
    }
}
