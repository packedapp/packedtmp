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
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactContext;
import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactSource;
import app.packed.base.Key;
import app.packed.component.CustomConfigurator;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerConfiguration;
import packed.internal.artifact.InstantiationContext;
import packed.internal.util.LookupUtil;

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
// hasService -> contains
// Description... hmm its just super helpful...
// Injector does not have a name. In many cases there are a container behind an Injector.
// But if, for example, a component has its own injector. That injector does not have a container behind it.

// Do we have an internal injector and an external injector?????
// Or maybe an Injector and an InternalInjector (which if exportAll is the same???)

// Altsaa den hoerer vel ikke til her...
// Vi kan jo injecte andre ting en services
public interface Injector {

    /**
     * Returns the configuration site of this injector.
     * 
     * @return the configuration site of this injector
     */
    ConfigSite configSite();

    /**
     * Returns the service contract of this injector. The returned contract will only have
     * {@link ServiceContract#services()} filled out.
     * 
     * @return the service contract of this injector
     */
    default ServiceContract contract() {
        return ServiceContract.newContract(c -> services().forEach(s -> c.addProvides(s.key())));
    }

    /**
     * Returns true if the injector contains a service with the specified key.
     *
     * @param key
     *            the key of the service
     * @return true if a service with the specified key exists.
     * @see #contains(Key)
     */
    default boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }

    /**
     * Returns {@code true} if a service with the specified key exists. Otherwise {@code false}.
     *
     * @param key
     *            the type of service
     * @return true if a service with the specified key exists. Otherwise false.
     * @see #contains(Class)
     */
    boolean contains(Key<?> key); // We do not call get here, as it might create a value

    /**
     * Returns a service instance for the given key if available, otherwise an empty optional. As an alternative, if you
     * know for certain that a service exists for the specified key, use {@link #use(Class)} for more fluent code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key for which to return a service instance
     * @return an optional containing the service instance if present, or an empty optional if not present
     * @see #use(Class)
     */
    default <T> Optional<T> get(Class<T> key) {
        return get(Key.of(key));
    }

    /**
     * Returns a service instance for the given key if available, otherwise an empty optional. As an alternative, if you
     * know for certain that a service exists for the specified key, use {@link #use(Class)} for more fluent code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key for which to return a service instance
     * @return an optional containing the service instance if present, or an empty optional if not present
     * @see #use(Class)
     */
    <T> Optional<T> get(Key<T> key);

    default <T> Optional<ServiceDescriptor> getDescriptor(Class<T> key) {
        return getDescriptor(Key.of(key));
    }

    default <T> Optional<ServiceDescriptor> getDescriptor(Key<T> key) {
        requireNonNull(key, "key is null");
        return services().filter(d -> d.key().equals(key)).findFirst();
    }

    // /**
    // * Injects services into the fields and methods of the specified instance.
    // * <p>
    // * This method is typically only needed if you need to construct objects yourself.
    // *
    // * @param <T>
    // * the type of object to inject into
    // * @param instance
    // * the instance to inject members (fields and methods) into
    // * @param lookup
    // * A lookup object used to access the various members on the specified instance
    // * @return the specified instance
    // * @throws InjectionException
    // * if any of the injectable members of the specified instance could not be injected
    // */

    // <T> T injectMembers(MethodHandles.Lookup caller, T instance);
    // <T> T injectMembers(T instance, MethodHandles.Lookup lookup);

    /**
     * Returns a unordered {@code Stream} of all services that this injector provides.
     *
     * @return a unordered {@code Stream} of all services that this injector provides
     */
    // services().service(s();

    default Set<ServiceDescriptor> descriptors() {
        throw new UnsupportedOperationException();
    }

    Stream<ServiceDescriptor> services();

    /**
     * Creates a new injector by specifying the downstream wirelets. Transform
     * <p>
     * Returns <code>this</code> if no wirelets are specified.
     * 
     * @param wirelets
     *            wirelets
     * @return the new injector
     */
    // Skal vi tage en Consumer<?>???? Saa faar vi en klasse med som kan sige noget om man er..
    // Det er taenkt paa en maade paa at alle Artifacts har et module de høre til...
    // Alternativet, er at man overtager
    Injector spawn(Wirelet... wirelets);

    /**
     * Returns a service of the specified type. Or throws an {@link UnsupportedOperationException} if this injector does not
     * provide a service with the specified key. The semantics method is identical to {@link #get(Class)} except that an
     * exception is thrown instead of returning if the service does not exist.
     *
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service for the specified key
     * @throws UnsupportedOperationException
     *             if no service with the specified key exist
     * @throws IllegalStateException
     *             if a service with the specified key exist, but the service has not been properly initialized yet. For
     *             example, if injecting an injector into a constructor of a service and then using the injector to try and
     *             access other service that have not been properly initialized yet. For example, a service that depends on
     *             the service being constructed
     * @see #contains(Class)
     */
    default <T> T use(Class<T> key) {
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
     * <pre>{@code
     * Key<WebServer> key = Key.of(WebServer.class);
     * injector.with(WebServer.class).printAllLiveConnections();}
     * </pre>
     *
     * Invoking this method is equivalent to:
     *
     * <pre>{@code
     *  Optional<T> t = get(key);
     *  if (!t.isPresent()) {
     *      throw new UnsupportedOperationException();
     *  }
     *  return t.get();}
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
     *             if a service with the specified key exist, but the service is not ready to be consumed yet. For example,
     *             if injecting an injector into a constructor of a service and then using the injector to try and access
     *             other service that have not been properly initialized yet. For example, a service that depends on the
     *             service being constructed
     */
    default <T> T use(Key<T> key) {
        Optional<T> t = get(key);
        if (!t.isPresent()) {
            throw new UnsupportedOperationException("A service with the specified key could not be found, key = " + key);
        }
        return t.get();
    }

    static ArtifactDriver<Injector> driver() {
        return InjectorArtifactHelper.DRIVER;
    }

    /**
     * Creates a new injector using a configurator object.
     *
     * @param configurator
     *            a consumer used for configuring the injector
     * @param wirelets
     *            wirelets
     * @return the new injector
     */
    // TODO I think move this to InjectorCongurator, InjectorConfigurator.spawn...
    // or maybe Injector.configure() instead
    // interface ArtifactConfigurator() {}
    // configure()
    static Injector configure(CustomConfigurator<? super InjectorAssembler> configurator, Wirelet... wirelets) {
        return driver().configure(ContainerConfiguration.driver(), c -> new InjectorAssembler(c), configurator, wirelets);
    }

    /**
     * Creates a new injector from the specified source.
     *
     * @param source
     *            the source to create the artifact from
     * @param wirelets
     *            various operations
     * @return the new injector
     * @throws RuntimeException
     *             if the injector could not be created for some reason. For example, if the source defines any components
     *             that requires a lifecycle
     */
    static Injector of(ArtifactSource source, Wirelet... wirelets) {
        return driver().instantiate(source, wirelets);
    }
}

// default Injector spawn(Wirelet... wirelets) {
// spawn = new injector + import...
// // Fucking ConfigSite...
// // if (wir)
// throw new UnsupportedOperationException();
// }

/** An artifact driver for creating {@link App} instances. */
final class InjectorArtifactHelper {

    static final MethodHandle CONV = LookupUtil.mhStaticSelf(MethodHandles.lookup(), "convert", Injector.class, ArtifactContext.class);

    static final ArtifactDriver<Injector> DRIVER = ArtifactDriver.of(MethodHandles.lookup(), Injector.class, CONV);

    static Injector convert(ArtifactContext container) {
        return ((InstantiationContext.PackedArtifactContext) container).injector();
    }
}
