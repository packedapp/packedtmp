/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import static app.packed.component.SidehandleBinding.Kind.FROM_CONTEXT;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.application.ApplicationInterface;
import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.assembly.Assembly;
import app.packed.bean.Bean;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.binding.Key;
import app.packed.binding.Provider;
import app.packed.component.SidehandleBinding;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.lifecycle.RunState;
import app.packed.operation.Op1;
import internal.app.packed.lifecycle.runtime.PackedExtensionContext;
import internal.app.packed.service.util.PackedServiceLocator;

/**
 * An service locator is a holder of services, where each service can be looked up by a {@link Key} at runtime.
 * <p>
 *
 *
 * An injector is typically created by populating an injector builder with the various services that needs to be
 * available.
 *
 * These
 *
 * _ xxxxx Injector controls the services that are available from every container at runtime and is typically used for
 * and for injection.
 *
 * Typically a number of injectors exist. The container injector is.
 *
 *
 *
 * Container injector can be.
 *
 * For example, the injector for a component will also include a Component service. Because an instance of the component
 * interface can always be injected into any method.
 *
 *
 * <p>
 * An Injector instance is usually acquired in one of the following three ways:
 * <h3>Directly from a Container instance</h3> By calling container.getService(ServiceManager.class)}:
 *
 * <pre>
 * Container c = ...;
 * Injector injector = c.getService(Injector.class);
 * IO.println(&quot;Available services: &quot; + Injector.services());
 * </pre>
 *
 * <h3>Annotated method, such as OnStart or OnStop</h3> When using annotations such as OnStart or OnStop. An injected
 * component manager can be used to determine which parameters are available for injection into the annotated method.
 *
 * <pre>
 * &#064;RunOnStart()
 * public void onStart(ServiceManager ServiceManager) {
 *     IO.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 * }
 * </pre>
 *
 * <h3>Injecting it into a Constructor</h3> Or, by declaring it as a parameter in the constructor of a service or agent
 * registered using container builder or container builder
 *
 * <pre>
 * public class MyService {
 *     public MyService(ServiceManager ServiceManager) {
 *         IO.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 *     }
 * }
 * </pre>
 *
 * <p>
 * The map returned by this method may vary doing the life cycle of a container. For example, if this method is invoked
 * in the constructor of a service registered with container builder. An instance of container builder is present in the
 * map returned. However, after the container has been initialized, the container will no longer keep a reference to the
 * configuration instance. So instances of Injector will never be available from any service manager after the container
 * has fully started.
 * <p>
 * Injectors are always immutable, however, extensions of this interface might provide mutable operations for methods
 * unrelated to injection.
 *
 * <p>
 * Unless otherwise specified the set of services provided by a service locator is unchangeable.
 */
@AutoInject(introspector = ServiceLocatorBeanIntrospector.class)
// I don't think this should have a service provider
public interface ServiceLocator extends ApplicationInterface {

    /**
     * Returns {@code true} if this service locator provides a service with the specified key.
     *
     * @param key
     *            key whose presence in this service locator is to be tested
     * @return {@code true} if a service with the specified key is provided by this service locator. Otherwise {@code false}
     * @see #contains(Key)
     */
    default boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }

    /**
     * Returns {@code true} if this service locator provides a service with the specified key.
     *
     * @param key
     *            key whose presence in this service locator is to be tested
     * @return {@code true} if a service with the specified key is provided by this service locator. Otherwise {@code false}
     * @see #contains(Class)
     */
    default boolean contains(Key<?> key) {
        requireNonNull(key, "key is null");
        return keys().contains(key);
    }

    /**
     * Returns a service instance with the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service is provided for the specified key, {@link #use(Class)} usually gives more
     * fluent code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key of the service to find
     * @return an optional containing an instance of the service if present, or an empty optional if not present
     * @see #use(Class)
     */
    default <T> Optional<T> find(Class<T> key) {
        return find(Key.of(key));
    }

    /**
     * Returns a service instance with the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service is provided for the specified key, {@link #use(Key)} usually gives more fluent
     * code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key of the service to find
     * @return an optional containing an instance of the service if present, or an empty optional if not present
     * @see #use(Key)
     */
    default <T> Optional<T> find(Key<T> key) {
        return findProvider(key).map(p -> p.provide());
    }

    default <T> Optional<Provider<T>> findProvider(Class<T> key) {
        return findProvider(Key.of(key));
    }

    <T> Optional<Provider<T>> findProvider(Key<T> key);

    /**
     * If a service with the specified key is present, performs the given action with a service instance, otherwise does
     * nothing.
     *
     * @param key
     *            the key to test
     * @param action
     *            the action to be performed, if a service with the specified key is present
     */
    default <T> void ifPresent(Class<T> key, Consumer<? super T> action) {
        ifPresent(Key.of(key), action);
    }

    /**
     * If a service with the specified key is provided, performs the given action with a service instance, otherwise does
     * nothing.
     *
     * @param key
     *            the key to test
     * @param action
     *            the action to be performed, if a service with the specified key is provided
     */
    default <T> void ifPresent(Key<T> key, Consumer<? super T> action) {
        requireNonNull(action, "action is null");
        Optional<T> o = find(key);
        if (o.isPresent()) {
            T instance = o.get();
            action.accept(instance);
        }
    }

    /** {@return true if this locator provides any services, otherwise false} */
    default boolean isEmpty() {
        return keys().isEmpty();
    }

    /**
     * Returns a set view containing the keys of every provided service.
     * <p>
     * If this locator supports removals, the returned set will also support removal operations: {@link Set#clear()},
     * {@link Set#remove(Object)}, {@link Set#removeAll(java.util.Collection)},
     * {@link Set#removeIf(java.util.function.Predicate)} and {@link Set#retainAll(java.util.Collection)}. or via any set
     * iterators. The returned map will never support insertion or update operations.
     * <p>
     * The returned set will retain any thread-safety guarantees provided by the locator itself.
     *
     * @return a set view containing the keys of every provided service.
     */
    Set<Key<?>> keys();

    /** {@return a service selection with all of the services in this locator} */
    ServiceSelection<?> selectAll();

    /**
     * Returns a service selection where the raw type of every service key is assignable to the specified type.
     * <p>
     * Unlike this method {@link #selectWithAnyQualifiers(Class)} this method will also select any
     *
     * @param <T>
     *            the assignable type
     * @param type
     *            the assignable type
     * @return the service selection
     */
    <T> ServiceSelection<T> selectAssignableTo(Class<T> type);

    /** {@return the number of services provided by this locator} */
    default int size() {
        return keys().size();
    }

    // toMapOfProviders
    default Map<Key<?>, Provider<?>> toProviderMap() {
        HashMap<Key<?>, Provider<?>> map = new HashMap<>();
        for (Key<?> key : keys()) {
            map.put(key, findProvider(key).get());
        }
        return Map.copyOf(map);
    }

    /**
     * Returns a service with the specified key. Or throws a {@link NoSuchElementException} if no such service is available.
     * <p>
     * The semantics of this method are identical to {@link #findInstance(Class)} except that an exception is thrown instead
     * of returning if the service does not exist.
     *
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service for the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     * @see #find(Class)
     */
    default <T> T use(Class<T> key) {
        return use(Key.of(key));
    }

    /**
     * Returns a service with the specified type, or throws a {@link NoSuchElementException} if no such service exists. This
     * method is typically used to create fluent APIs such as:
     *
     * <pre>{@code
     * Key<WebServer> key = Key.of(WebServer.class);
     * locator.use(key).printAllLiveConnections();}
     * </pre>
     *
     * The default implementation of this method does:
     *
     * <pre>{@code
     *  Optional<T> t = find(key);
     *  if (!t.isPresent()) {
     *      throw new NoSuchElementException();
     *  }
     *  return t.get();}
     * </pre>
     *
     * @param <T>
     *            the type of service instance this method returns
     * @param key
     *            the key of the service instance to return
     * @return a service instance for the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     */
    default <T> T use(Key<T> key) {
        Optional<T> t = find(key);
        if (!t.isPresent()) {
            throw new NoSuchElementException("A service with the specified key does not exist, key = " + key);
        }
        return t.get();
    }

    /**
     * Returns the bootstrap app. If interfaces allowed non-public fields we would have stored it in a field instead of this
     * method.
     */
    private static BootstrapApp<ServiceLocator> bootstrap() {
        // ServiceLocatorBootstrap because we cannot have private static fields on interfaces
        class ServiceLocatorBootstrap {
            private static final BootstrapApp<ServiceLocator> APP = BootstrapApp
                    .ofUnmanaged(Bean.<ServiceLocator>of(new Op1<@SidehandleBinding(FROM_CONTEXT) ServiceLocator, ServiceLocator>(e -> e) {}));
        }
        return ServiceLocatorBootstrap.APP;
    }

    /**
     * Creates a new service locator image from the specified assembly and optional wirelets.
     *
     * @param assembly
     *            the assembly to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     */
    static ServiceLocator.Image imageOf(Assembly assembly, Wirelet... wirelets) {
        return new ServiceLocator.Image(bootstrap().imageOf(assembly, wirelets));
    }

    /**
     * Creates a new application mirror from the specified assembly and optional wirelets.
     *
     * @param assembly
     *            the assembly to use for creating the mirror
     * @param wirelets
     *            optional wirelets
     * @return the new application mirror
     */
    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return bootstrap().mirrorOf(assembly, wirelets);
    }

    /** {@return an empty service locator that provides no services.} */
    static ServiceLocator of() {
        return new PackedServiceLocator(PackedExtensionContext.EMPTY, Map.of());
    }

    /**
     * Creates a new service locator from the specified assembly and optional wirelets.
     *
     * @param assembly
     *            the assembly that should be used to create the service locator
     * @param wirelets
     *            optional wirelets
     * @return a new service locator
     */
    static ServiceLocator of(Assembly assembly, Wirelet... wirelets) {
        return bootstrap().launch(RunState.INITIALIZED, assembly, wirelets);
    }


    static void verify(Assembly assembly, Wirelet... wirelets) {
        bootstrap().verify(assembly, wirelets);
    }

    /** An application image for App. */
    public static final class Image {

        /** The bootstrap image we are delegating to */
        private final BootstrapApp.Image<ServiceLocator> image;

        private Image(BootstrapApp.Image<ServiceLocator> image) {
            this.image = image;
        }

        /** Creates a new service locator application from this image. */
        public ServiceLocator initialize() {
            return image.initialize();
        }
    }
}

final class ServiceLocatorBeanIntrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        // TODO: implement ServiceLocator binding
        super.onExtensionService(key, service);
    }
}
