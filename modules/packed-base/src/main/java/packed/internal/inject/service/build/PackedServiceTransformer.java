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
import app.packed.inject.Factory;
import app.packed.inject.Service;
import app.packed.inject.ServiceLocator;
import app.packed.inject.ServiceTransformer;
import packed.internal.inject.service.AbstractService;
import packed.internal.inject.service.AbstractServiceRegistry;
import packed.internal.inject.service.runtime.PackedInjector;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;
import packed.internal.util.ForwardingMap;
import packed.internal.util.ForwardingStrategy;

/** Implementation of {@link ServiceTransformer}. */
public final class PackedServiceTransformer extends AbstractServiceRegistry implements ServiceTransformer {

    /** A lazily initialized map that is exposed via {@link #asMap}. */
    private ForwardingMap<Key<?>, Service> asMap;

    /** The services that we do in-place transformation of. */
    // Maaske er det altid build time services???
    private final Map<Key<?>, AbstractService> services;

    @SuppressWarnings("unchecked")
    private PackedServiceTransformer(Map<Key<?>, ? extends AbstractService> services) {
        this.services = (Map<Key<?>, AbstractService>) requireNonNull(services);
    }

    private void add(Factory<?> factory, boolean auto, boolean isConstant, boolean replace) {
        requireNonNull(factory, "factory is null");
        // Det er saa her hvor vi skal til at bruge en realm...
        // Altsaa der er vel ingen grund til vi ikke checker allerede nu...
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<Key<?>, Service> asMap() {
        Map<Key<?>, Service> es;
        // TODO fix forwarding map for remove
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

    /** {@inheritDoc} */
    @Override
    public void map(Factory<?> factory) {
        add(factory, true, false, false);
    }

    /** {@inheritDoc} */
    @Override
    public void prototype(Factory<?> factory) {
        add(factory, false, false, false);
    }

    /** {@inheritDoc} */
    @Override
    public void provide(Factory<?> factory) {
        add(factory, false, true, false);
    }

    @Override
    public <T> void provideInstance(Key<T> key, T instance) {
        requireNonNull(key, "key is null");
        requireNonNull(instance, "instance is null");
        // instance must be assignable to key raw type, maybe the build entry should check that...
        services.put(key, new ConstantBuildtimeService(key, instance));
    }

    /** {@inheritDoc} */
    @Override
    public void rekey(Key<?> existingKey, Key<?> newKey) {
        requireNonNull(existingKey, "existingKey is null");
        requireNonNull(newKey, "newKey is null");
        if (existingKey.equals(newKey)) {
            throw new IllegalStateException("Cannot rekey to the same key, key = " + existingKey);
        } else if (services.containsKey(newKey)) {
            throw new IllegalStateException("A service with newKey already exists, newKey = " + newKey);
        }
        services.put(newKey, checkKeyExists(existingKey).rekeyAs(newKey));
    }

    /** {@inheritDoc} */
    @Override
    public void rekeyAll(Function<Service, @Nullable Key<?>> function) {
        requireNonNull(function, "function is null");

        // We don't replace in-map as we want to be able to swap keys.
        // Which would not be possible if we did it in place.
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
                    throw new IllegalStateException("Another service was already rekeyed to the same key, key = " + key);
                }
            }
        }
        services.clear();
        services.putAll(newServices);
    }

    /** {@inheritDoc} */
    @Override
    public void replace(Factory<?> factory) {
        add(factory, true, false, true);
    }

    /**
     * Creates a new service locator.
     * 
     * @return the new service locator
     */
    public static ServiceLocator toServiceLocator(Map<Key<?>, ? extends AbstractService> services, Consumer<? super ServiceTransformer> transformation) {
        requireNonNull(transformation, "transformation is null");
        PackedServiceTransformer psm = new PackedServiceTransformer(services);
        transformation.accept(psm);

        Map<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext();
        for (AbstractService e : services.values()) {
            runtimeEntries.put(e.key(), ((BuildtimeService) e).toRuntimeEntry(con));
        }
        return new PackedInjector(ConfigSite.UNKNOWN, runtimeEntries);
    }

    public static ServiceLocator transform(Consumer<? super ServiceTransformer> transformation, Collection<RuntimeService> services) {
        requireNonNull(transformation, "transformation is null");
        HashMap<Key<?>, BuildtimeService> m = new HashMap<>();
        for (RuntimeService s : services) {
            m.put(s.key(), new RuntimeAdaptorBuildtimeService(ConfigSite.UNKNOWN, s));
        }
        return toServiceLocator(m, transformation);
    }

    public static void transformInplace(Map<Key<?>, ? extends AbstractService> services, Consumer<? super ServiceTransformer> transformer) {
        PackedServiceTransformer dst = new PackedServiceTransformer(services);
        transformer.accept(dst);
    }

    public static <T> void transformInplaceAttachment(Map<Key<?>, ? extends AbstractService> services,
            BiConsumer<? super ServiceTransformer, ? super T> transformer, T attachment) {
        PackedServiceTransformer dst = new PackedServiceTransformer(services);
        transformer.accept(dst, attachment);
    }
}
