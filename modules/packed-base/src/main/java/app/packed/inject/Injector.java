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

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.bundle.BundleConfigurationContext;
import app.packed.bundle.WiringOperation;
import app.packed.config.ConfigurationSite;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Taggable;
import packed.internal.bundle.Bundles;
import packed.internal.inject.builder.InjectorBuilder;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * An injector is an immutable holder of services that can be dependency injected or looked up by their type at runtime.
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
 * System.out.println(&quot;Available services: &quot; + Injector.services());
 * </pre>
 *
 * <h3>Annotated method, such as OnStart or OnStop</h3> When using annotations such as OnStart or OnStop. An injected
 * component manager can be used to determine which parameters are available for injection into the annotated method.
 *
 * <pre>
 * &#064;RunOnStart()
 * public void onStart(ServiceManager ServiceManager) {
 *     System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 * }
 * </pre>
 *
 * <h3>Injecting it into a Constructor</h3> Or, by declaring it as a parameter in the constructor of a service or agent
 * registered using container builder or container builder
 *
 * <pre>
 * public class MyService {
 *     public MyService(ServiceManager ServiceManager) {
 *         System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
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
 */
// getProvider(Class|Key|InjectionSite)
// get(InjectionSite)
// getService(Class|Key) .get(InjectionSite)<---Nah
public interface Injector extends Taggable {

    /**
     * Returns a service instance for the given injection key if available, otherwise an empty optional. If you know for
     * certain that a service exists for the specified, {@link #with(Class)} usually leads to prettier code.
     *
     * @param <T>
     *            the type of service this method returns
     * @param key
     *            the key for which to return a service instance
     * @return an optional containing the service instance if present, otherwise an empty optional
     * @see #with(Class)
     */
    default <T> Optional<T> get(Class<T> key) {
        requireNonNull(key, "key is null");
        return get(Key.of(key));
    }

    <T> Optional<T> get(Key<T> key);

    /**
     * Returns the configuration site of this injector.
     * 
     * @return the configuration site of this injector
     */
    ConfigurationSite getConfigurationSite();

    /**
     * Returns the (nullable) description of this injector. Or null if no description was set when it was configured.
     *
     * @return the (nullable) description of this injector. Or null if no description was set when it was configured.
     * @see InjectorConfiguration#setDescription(String)
     */
    @Nullable
    String getDescription();

    @Nullable
    default <T> ServiceDescriptor getService(Class<T> serviceType) {
        return getService(Key.of(serviceType));
    }

    @Nullable
    default <T> ServiceDescriptor getService(Key<T> key) {
        requireNonNull(key, "key is null");
        Optional<ServiceDescriptor> o = services().filter(d -> d.getKey().equals(key)).findFirst();
        return o.orElse(null);
    }

    /**
     * Returns true if a service matching the specified type exists. Otherwise false.
     *
     * @param key
     *            the type of service
     * @return true if a service matching the specified type exists. Otherwise false.
     * @see #hasService(Key)
     */
    default boolean hasService(Class<?> key) {
        requireNonNull(key, "key is null");
        return hasService(Key.of(key));
    }

    /**
     * Returns {@code true} if a service matching the specified type exists. Otherwise {@code false}.
     *
     * @param key
     *            the type of service
     * @return true if a service matching the specified type exists. Otherwise false.
     * @see #hasService(Class)
     */
    boolean hasService(Key<?> key); // We do not call get here, as it might create a value

    /**
     * Injects services into the fields and methods of the specified instance.
     * <p>
     * This method is typically only needed if need to construct objects yourself.
     *
     * @param <T>
     *            the type of object to inject into
     * @param instance
     *            the instance to inject members (fields and methods) on
     * @param lookup
     *            A lookup object
     * @return the specified instance
     * @throws InjectionException
     *             if any of the injectable members of the specified instance could not be injected
     */
    <T> T injectMembers(T instance, MethodHandles.Lookup lookup);

    /**
     * Returns a unordered {@code Stream} over all the services that are available from this injector.
     *
     * @return a unordered {@code Stream} over all the services that are available from this injector
     */
    Stream<ServiceDescriptor> services();

    /**
     * Returns a service of the specified type. Or throws an {@link UnsupportedOperationException} if no service of the
     * specified type is available. The semantics method is identical to {@link #get(Class)} except that an exception is
     * thrown instead of returning if the service does not exist.
     *
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws UnsupportedOperationException
     *             if no service with the specified key exist
     * @throws IllegalStateException
     *             if a service with the specified key exist, but the service has not been properly initialized yet. For
     *             example, if injecting an injector into a constructor of a service and then using the injector to try and
     *             access other service that have not been properly initialized yet. For example, a service that depends on
     *             the service being constructed
     * @see #hasService(Class)
     */
    default <T> T with(Class<T> key) {
        Optional<T> t = get(key);
        if (!t.isPresent()) {
            throw new UnsupportedOperationException("A service with the specified key could not be found, key = " + key);
        }
        return t.get();
    }

    /**
     * Returns a service with the specified type, or throws an {@link UnsupportedOperationException} if no such service
     * exists. This is typically used to create fluent APIs such as:
     *
     * <pre>
     * injector.with(WebServer.class).printAllLiveConnections();
     * </pre>
     *
     * Invoking this method is roughly equivalent to:
     *
     * <pre>
     * if (!injector.hasService(type)) {
     *     throw new UnsupportedOperationException("A service with the specified key could not be found, key = " + type);
     * }
     * return injector.get(type);
     * </pre>
     *
     * @param <T>
     *            the type of service this method returns
     * @param key
     *            the key of the service to return
     * @return a service with the specified key
     * @throws UnsupportedOperationException
     *             if no service with the specified key could be found
     * @throws IllegalStateException
     *             if a service with the specified key exist, but the service has not been properly initialized yet. For
     *             example, if injecting an injector into a constructor of a service and then using the injector to try and
     *             access other service that have not been properly initialized yet. For example, a service that depends on
     *             the service being constructed
     */
    default <T> T with(Key<T> key) {
        Optional<T> t = get(key);
        if (!t.isPresent()) {
            throw new UnsupportedOperationException("A service with the specified key could not be found, key = " + key);
        }
        return t.get();
    }

    /**
     * Creates a new injector via the specified configurator.
     *
     * @param configurator
     *            a consumer used for configuring the injector
     * @return the new injector
     */
    // Maa have noget Bootstrap??? ogsaa med stages o.s.v., vi gider ihvertfald ikke bliver noedt til at lave en
    // container hvor vi skal importere den anden container. Og saa staar vi ved med to.
    // Nej vi skal have noget boot agtigt noget. InjectorBooter?
    // /* , Object... requirements */
    static Injector of(Consumer<InjectorConfiguration> configurator) {
        requireNonNull(configurator, "configurator is null");
        InjectorBuilder builder = new InjectorBuilder(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF));
        configurator.accept(builder);
        return builder.build();
    }

    /**
     * Creates a new injector from the specified bundle.
     *
     * @param bundle
     *            a bundle to create an injector from
     * @return the new injector
     */
    static Injector of(InjectorBundle bundle, WiringOperation... operations) {
        requireNonNull(bundle, "bundle is null");
        InjectorBuilder builder = new InjectorBuilder(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF), bundle);

        BundleConfigurationContext bs = new BundleConfigurationContext() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T with(Class<? super T> type) {
                if (type == InjectorBuilder.class) {
                    return (T) builder;
                }
                return super.with(type);
            }
        };
        bs.configure(bundle);

        // BundleSupport.invoke().configureInjectorBundle(bundle, builder, true);
        return builder.build();
    }

    static Injector of(Class<? extends InjectorBundle> bundleType, WiringOperation... operations) {
        return of(Bundles.instantiate(bundleType), operations);
    }

    default void print() {
        services().forEach(s -> System.out.println(s));
    }
}

/// **
// * Creates a new injector builder with this injector with all the services available in this injector also be
/// available
// * in the new injector. To override existing service in this injector use {@link #spawn(Consumer, Predicate)} and
/// remove
// * the service you do not want via a filter Put on Injectors???
// *
// * @return a new injector builder
// */
// default Injector spawnInjector(Consumer<? super InjectorConfiguration> configurator) {
// // Nej vil sgu have det så man let kan overskrive dem.
// // Bliver noedt til at have noget special support.
// // Maa smide dem alle ind i
//
// // Would also be nice with a filtered injector, but spawn(c->{}, filter) will do it for now
// requireNonNull(configurator, "configurator is null");
// return Injector.of(c -> {
// c.importInjector(this);
// configurator.accept(c);
// });
// }
