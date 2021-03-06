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

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Qualifier;
import app.packed.component.BeanDriver;
import app.packed.component.BeanType;
import app.packed.component.ComponentDriver;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.inject.Factory;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import app.packed.lifecycle.OnStart;
import app.packed.validate.Validator;
import packed.internal.component.bean.PackedBeanDriverBinder;
import packed.internal.container.ExtensionSetup;
import packed.internal.service.ServiceManagerSetup;
import packed.internal.service.runtime.AbstractServiceLocator;
import packed.internal.service.sandbox.InjectorComposer;

/**
 * An extension that deals with the service functionality of a container.
 * <p>
 * This extension provides the following functionality:
 * 
 * is extension provides functionality for exposing and consuming services.
 * 
 * 
 */
// Functionality for
// * Explicitly requiring services: require, requiOpt & Manual Requirements Management
// * Exporting services: export, exportAll
// * Providing components or injectors (provideAll)
// * Manual Injection

// Har ikke behov for delete fra ServiceLocator

// Future potential functionality
/// Contracts
/// Security for public injector.... Maaske skal man explicit lave en public injector???
/// Transient requirements Management (automatic require unresolved services from children)
/// Integration pits
// MHT til Manuel Requirements Management
// (Hmm, lugter vi noget profile?? Nahh, folk maa extende BaseAssembly og vaelge det..
// Hmm saa auto instantiere vi jo injector extensionen
//// Det man gerne vil kunne sige er at hvis InjectorExtensionen er aktiveret. Saa skal man
// altid bruge Manual Requirements
// contracts bliver installeret direkte paa ContainerConfiguration

// Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
// Maybe have an Container.onExtensionActivation(Extension e) <- man kan overskrive....
// Eller @ContainerStuff(onActivation = FooActivator.class) -> ForActivator extends ContainerController

// Taenker den kun bliver aktiveret hvis vi har en factory med mindste 1 unresolved dependency....
// D.v.s. install(Class c) -> aktivere denne extension, hvis der er unresolved dependencies...
// Ellers selvfoelgelig hvis man bruger provide/@Provides\
public class ServiceExtension extends Extension {

    /** A binder for prototype service beans.  */
    @SuppressWarnings("rawtypes")
    private static final BeanDriver.Binder PROTOTYPE_SERVICE_BEAN_BINDER = PackedBeanDriverBinder.of(MethodHandles.lookup(), ServiceBeanConfiguration.class, BeanType.PROTOTYPE_UNMANAGED);

    /** A binder for singleton service beans.  */
    @SuppressWarnings("rawtypes")
    private static final BeanDriver.Binder SINGLETON_SERVICE_BEAN_BINDER = PackedBeanDriverBinder.of(MethodHandles.lookup(), ServiceBeanConfiguration.class, BeanType.BASE);

    /** The service manager. */
    private final ServiceManagerSetup services;

    /**
     * Create a new service extension.
     * 
     * @param setup
     *            an extension setup object (hidden).
     */
    /* package-private */ ServiceExtension(ExtensionSetup setup) {
        this.services = setup.container.injection.newServiceManagerFromServiceExtension();
    }

    // Validates the outward facing contract
    public void checkContract(Validator<? super ServiceContract> validator) {
        // Hmm maaske man ville lave et unit test istedet for...
    }

    public void checkExactContract(ServiceContract sc) {
        checkContract(ServiceContractChecker.exact(sc));
    }

    /**
     * Exports a service of the specified type. See {@link #export(Key)} for details.
     * 
     * @param <T>
     *            the type of service to export
     * @param key
     *            the key of the service to export
     * @return a configuration for the exported service
     * @see #export(Key)
     * @see #exportAll()
     */
    public <T> ExportedServiceConfiguration<T> export(Class<T> key) {
        return export(Key.of(key));
    }

    /**
     * Exports an internal service outside of this container.
     * 
     * <pre>
     *  {@code  
     * install(ServiceImpl.class);
     * export(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * 
     * <pre>
     *  {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);}
     * </pre>
     * 
     * <p>
     * Packed does not support any other way of exporting a service provided via a field or method annotated with
     * {@link Provide} except for this method. There are no plan to add an Export annotation that can be used in connection
     * with {@link Provide}.
     * 
     * <p>
     * A service can be exported multiple times
     * 
     * @param <T>
     *            the type of the service to export
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    // Is used to export @Provide method and fields.
    public <T> ExportedServiceConfiguration<T> export(Key<T> key) {
        requireNonNull(key, "key is null");
        checkIsPreCompletion();
        return services.exports().export(key /* , captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE) */);
    }

    // Altsaa skal vi hellere have noget services().filter().exportall();

    // Alternativ this export all er noget med services()..

    // is exportAll applied immediately or at the end???

    // Maaske er den mest brugbart hvis den bliver applied nu!
    // Fordi saa kan man styre ting...
    // F.eks. definere alt der skal exportes foerst
    // Den er isaer god inde man begynder at linke andre containere

    // One of 3 models...
    // Fails on other exports
    // Ignores other exports
    // interacts with other exports in some way
    /**
     * Exports all container services and any services that have been explicitly anchored via of anchoring methods.
     * <p>
     * 
     * <ul>
     * <li><b>Service already exported.</b> The service that have already been exported (under any key) are always
     * ignored.</li>
     * <li><b>Key already exported.</b>A service has already been exported under the specified key.
     * <li><b>Are requirements.</b> Services that come from parent containers are always ignored.</li>
     * <li><b>Not part of service contract.</b> If a service contract has set. Only services for whose key is part of the
     * contract is exported.</li>
     * </ul>
     * <p>
     * This method can be invoked more than once. But use cases for this are limited.
     */
    public void exportAll() {
        // Tror vi aendre den til streng service solve...
        // Og saa tager vi bare alle services() og exportere

        // Add exportAll(Predicate); //Maybe some exportAll(Consumer<ExportedConfg>)
        // exportAllAs(Function<?, Key>

        // Export all entries except foo which should be export as Boo
        // exportAll(Predicate) <- takes key or service configuration???

        // export all _services_.. Also those that are already exported as something else???
        // I should think not... Det er er en service vel... SelectedAll.keys().export()...
        checkIsPreCompletion();
        services.exports().exportAll( /* captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE) */);
    }

    /** {@return a mirror for this extension.} */
    @Override
    protected ServiceExtensionMirror mirror() {
        return mirrorInitialize(new ServiceExtensionMirror(services));
    }

    /**
     * Binds the specified implementation as a new service. The runtime will use {@link Factory#of(Class)} to find a valid
     * constructor or method to instantiate the service instance once the injector is created.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the {@code Class} is annotated with
     * a {@link Qualifier qualifier annotation}, the default key will have the qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param implementation
     *            the implementation to bind
     * @return a service configuration for the service
     * @see InjectorComposer#provide(Class)
     */
    public <T> ServiceBeanConfiguration<T> provide(Class<T> implementation) {
        // Create a bean driver by binding the implementation
        @SuppressWarnings("unchecked")
        ComponentDriver<ServiceBeanConfiguration<T>> driver = SINGLETON_SERVICE_BEAN_BINDER.bind(implementation);

        return context().wire(driver).provide();
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provide}.
     *
     * @param <T>
     *            the type of component to install
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    public <T> ServiceBeanConfiguration<T> provide(Factory<T> factory) {
        // Create a bean driver by binding a factory
        @SuppressWarnings("unchecked")
        ComponentDriver<ServiceBeanConfiguration<T>> driver = SINGLETON_SERVICE_BEAN_BINDER.bind(factory);

        return context().wire(driver).provide();
    }

    /**
     * Provides every service from the specified locator.
     * 
     * @param locator
     *            the locator to provide services from
     * @throws IllegalArgumentException
     *             if the specified locator is not implemented by Packed
     */
    public void provideAll(ServiceLocator locator) {
        requireNonNull(locator, "locator is null");
        if (!(locator instanceof AbstractServiceLocator l)) {
            throw new IllegalArgumentException("Custom implementations of " + ServiceLocator.class.getSimpleName()
                    + " are currently not supported, locator type = " + locator.getClass().getName());
        }
        checkIsPreCompletion();
        services.provideAll(l);
    }

    /**
     * Binds a new service constant to the specified instance.
     * <p>
     * The default key for the service will be {@code instance.getClass()}. If the type returned by
     * {@code instance.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will have the
     * qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param instance
     *            the instance to bind
     * @return a service configuration for the service
     */
    public <T> ServiceBeanConfiguration<T> provideInstance(T instance) {
        // Create the bean driver by binding the implementation
        @SuppressWarnings("unchecked")
        ComponentDriver<ServiceBeanConfiguration<T>> driver = SINGLETON_SERVICE_BEAN_BINDER.bindInstance(instance);

        return wire(driver).provide();
    }

    public <T> ServiceBeanConfiguration<T> providePrototype(Class<T> implementation) {
        // Create a bean driver by binding the implementation
        @SuppressWarnings("unchecked")
        ComponentDriver<ServiceBeanConfiguration<T>> driver = PROTOTYPE_SERVICE_BEAN_BINDER.bind(implementation);

        return wire(driver);
    }

    public <T> ServiceBeanConfiguration<T> providePrototype(Factory<T> factory) {
        // Create a new bean driver by by binding the factory
        @SuppressWarnings("unchecked")
        ComponentDriver<ServiceBeanConfiguration<T>> driver = PROTOTYPE_SERVICE_BEAN_BINDER.bind(factory);

        return wire(driver);
    }

    // requires bliver automatisk anchoret...
    // anchorAllChildExports-> requireAllChildExports();
    public void require(Class<?>... keys) {
        require(Key.of(keys));
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
    public void require(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsPreCompletion();
        // ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE);
        for (Key<?> key : keys) {
            services.dependencies().require(key, false /* , cs */);
        }
    }

    public void requireOptionally(Class<?>... keys) {
        requireOptionally(Key.of(keys));
    }

    /**
     * Adds the specified key to the list of optional services.
     * <p>
     * If a key is added optionally and the same key is later added as a normal (mandatory) requirement either explicitly
     * via # {@link #require(Key...)} or implicitly via, for example, a constructor dependency. The key will be removed from
     * the list of optional services and only be listed as a required key.
     * 
     * @param keys
     *            the key(s) to add
     */
    // How does this work with child services...
    // They will be consumed
    public void requireOptionally(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        checkIsPreCompletion();
        // ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE_OPTIONAL);
        for (Key<?> key : keys) {
            services.dependencies().require(key, true /* , cs */);
        }
    }

    public ServiceRegistry services() {
        // Why not composer? Or at least mutable Service registry
        throw new UnsupportedOperationException();
    }

    /**
     * Performs a final transformation of any exported service.
     * 
     * This method can perform any final adjustments of services before they are made available to any parent container.
     * <p>
     * The transformation takes place xxxx
     * 
     * @param transformer
     *            transforms the exported services
     */
    public void transformExports(Consumer<? super ServiceComposer> transformer) {
        services.exports().setExportTransformer(transformer);
    }

    /**
     * A subtension that can be used by other extensions via {@link Extension#use(Class)} or
     * {@link ExtensionContext#use(Class)}.
     * <p>
     * This class does not provide any support for exporting services. The end-user is always in full control of exactly
     * what is being exported out from the container.
     **/
    // Tror slet vi supportere at extension kan provide services....
    public class Sub extends Subtension {

        // Require???
        // This is very limited as we can only require services from other
        // extensions that we depend on...

        // SetContract
        /// This would apply locally to the extension...
        /// limited usefullness

        /** The requesting extension. */
        final Class<? extends Extension> requestingExtension;

        /**
         * Creates a new sub.
         * 
         * @param requestingExtension
         *            the requesting extension
         */
        /* package-private */ Sub(Class<? extends Extension> requestingExtension) {
            this.requestingExtension = requireNonNull(requestingExtension, "requestingExtension is null");
        }

        public void check() {
            System.out.println("Requested by " + requestingExtension);
        }
    }
}

class ServiceExtensionBadIdeas {
    // Syntes anchorAll paa selve extensionen er en daarlig ide...

//  /**
//   * 
//   * This method is typically used if you work with plugin structures. Where you do not now ahead of time what kind of
//   * services the plugins export.
//   * <p>
//   * If you only want to anchor ind.... {@link ServiceSelection}
//   * 
//   * @see ServiceWirelets#anchorAll()
//   */
//  // export does not anchor I think..
//  // only if a service...
//  public void anchorAll() {
//      anchorIf(t -> true);
//  }
//
//  /**
//   * @param filter
//   *            the filter
//   * @see ServiceWirelets#anchorIf(Predicate)
//   */
//  @SuppressWarnings({ "rawtypes", "unchecked" })
//  public void anchorIf(Predicate<? super Service> filter) {
//      requireNonNull(filter, "filter is null");
//      Predicate<? super Service> a = services.anchorFilter;
//      services.anchorFilter = a == null ? filter : ((Predicate) a).or(filter);
//  }

    //
//  // Hmm hvis vi gerne vil smide attributer paa...
//  // Maaske har vi en for each der giver en ServiceConfiguration som man kan kalde export paa...
//  public void exportAll(Function<? super Service, @Nullable Key<?>> exportFunction) {
//      services().forEach(s -> export(s.key()));
//  }
//
//  public void exportIf(Predicate<? super Service> filter) {
//      services().services().filter(filter).forEach(s -> export(s.key()));
//      // Only anchrored services??? Yes
//      // ContainerServices and Ac
//  }

}

///**
//* Expose a service contract as an attribute.
//* 
//* @return a service contract for this extension
//*/
//@ExposeAttribute(declaredBy = ServiceAttributes.class, name = "contract")
///* package-private */ ServiceContract exposeContract() {
// return services.newServiceContract();
//}
//
///**
//* Exposes an immutable service registry of all exported services. Or null if there are no exports.
//* 
//* @return any exported services. Or null if there are no exports
//*/
//@ExposeAttribute(declaredBy = ServiceAttributes.class, name = "exported-services", optional = false)
//@Nullable
///* package-private */ ServiceRegistry exposeExportedServices() {
//
// // Kan specificere det paa attributen???
// // Giv mig en ServiceExtension... og saa skal jeg vise dig...
// // Det kraever jo vi force loader den...
//
//// $addAttribute(ServiceExtension.class, ServiceAttributes.EXPORTED_SERVICES, s -> s.services.exports().exportsAsServiceRegistry());
//// $addOptionalAttribute(ServiceExtension.class, ServiceAttributes.EXPORTED_SERVICES, s -> s.services.exports().hasExports());
////
//// $attribute(ServiceExtension.class, a -> {
////     a.add(EXPORTED_SERVICES, s -> s.services.exports().exportsAsServiceRegistry());
////     a.optional(EXPORTED_SERVICES, p -> p.services.exports().hasExports(), s -> s.services.exports().exportsAsServiceRegistry());
//// });
////
//// AttributeMaker<ServiceExtension> a = $attribute(ServiceExtension.class);
//// a.add(EXPORTED_SERVICES, s -> s.services.exports().exportsAsServiceRegistry());
//// a.optional(EXPORTED_SERVICES, p -> p.services.exports().hasExports(), s -> s.services.exports().exportsAsServiceRegistry());
//
// return services.exports().exportsAsServiceRegistry();
//}
class ZExtraFunc {

    protected void addAlias(Class<?> existing, Class<?> newKey) {}

    protected void addAlias(Key<?> existing, Key<?> newKey) {}

    // Maaske er det her mere injection then service

    // Vi vil gerne tilfoejer
//    protected ExportedServiceConfiguration<ServiceSelection> addSelectin(Function<>) {}

    <T> ServiceConfiguration<T> addOptional(Class<T> optional) {
        // @Inject is allowed, but other annotations, types und so weiter is not...
        // Den har ihvertfald slet ikke noget providing...
        throw new UnsupportedOperationException();
    }

    <T> ServiceConfiguration<T> alias(Class<T> key) {
        // Hmm maaske vi skal kalde den noget andet...
        // SingletonService kan sikkert sagtens extende den...
        // ProtoypeConfiguration has altid en noegle og ikek optional..
        throw new UnsupportedOperationException();
    }

    <T> ServiceConfiguration<T> assistedInject(Class<T> interfase) {
        // Only support interface to start with
        // Will never try and implement default methods

        // or abstract class can have state which can be merge in some way?
        // well def not ver 1.

        // GeneratingComponentConfiguration

        // userWire

        throw new UnsupportedOperationException();
    }

    // Det er jo i virkeligheden bare en @RunOnInjection klasse
    // LifecycleExtension-> BreakCycle(X,Y) ->
    <S, U> void cycleBreaker(Class<S> keyProducer, Class<U> key2) {
        // keyProducer will have a Consumer<U> injected in its constructor.
        // In which case it must call it exactly once with a valid instance of U.
        // U will then be field/method inject, initialization und so weither as normally.
        // But to the outside it will not that S depends on U.

        // Den der tager en biconsumer supportere ikke at de kan vaere final fields af hinanden...

        // Det kan ogsaa vaere en klasse CycleBreaker.. som tager ContainerConfiguration
        // Container, Service Extension, ExtensionContext ect...

        // DE her virker kun indefor samme container...

        // Syntes i virkeligheden consumer versionen er bedre....
        // Den er meget mere explicit.
        // Den her skal jo pakke initialisering af U ind i en consumer
    }

//    <S, U> void breakCycle(OP2<S, U> op) {
//        // Denne kraever at vi paa en eller anden maade kan bruge OP2...
//        // MethodHandle op.invoker() <--- Saa maaske er det bare ikke hemmeligt mere.
//        // Eller kan bruge det...
//        throw new UnsupportedOperationException();
//    }

    <S, U> void cycleBreaker(Class<S> key1, Class<U> key2, BiConsumer<S, U> consumer) {

        // Taenker om vi skal checke at key2 depender on key1...
        // Jeg taenker ja, fordi saa saa kan vi visuelleciere det...
        // Og vi fejler hvis der ikke er en actuel dependency

        // Maaske bare warn istedet for at fejle. Men syntes ikke folk skal have en masse af dem liggende...

//        Break circular references...
//        runOnInitialize(ST2<Xcomp, YComp>(xComp.setY(yComp){});
//        Ideen er at vi har super streng cyclic check...
//        Og saa kan man bruge saadan en her til at bende det...

        // Maaske har vi endda en eksplicit i ServiceManager...
        // breakCycle, breakDependencyCycle
        // cyclicBreak(Op2(X, Y) -> x.setY(y)

        // Alternativt vil vi godt sige at X laver en ny Y component (ikke bare en service)

        // Will probably validate if there is an actual cycle...

//        Of course the idea is that we want to be very explicit about the eyesoar
//        So people can find it...

        // In a perfect world we would also write perfect cycle free code..
        // But to support your petty little program

        // http://blog.jdevelop.eu/?p=382
        // We don't support this. Server must instantiate the Client if it needs it
        // But then client isn't managed!!!

        // Could we also allow the user, to provide another OP???
        // That server could get which it could invoke
        // FN<Client, Server, String> : from server
        // this.client = fn.invoke(this, "fooobar");
        // Hmm, ikke saerlig paen... Men hvis vi vil have final/final
        // Vi tillader kun et object... Hvis man vil have flere.
        // Maa man pakke det ind i en Composite... I saa fald.. Vil det se ud som om
        // At det faktisk er client'en der kalder ting. F.eks. kunne man have en logger.... Som stadig ville se
        // client og ikke server. Selvom man kaldte gemmen servers constructor
        // CycleBreak
        // CConf c= install(Server.class)
        // c.assistInstall(client, new FN2<Client, Server, @Composite SomeRecord(Logger, RandomOtherThing))

        // Break down circles manuel.. https://github.com/google/guice/wiki/CyclicDependencies
        // Using decomposition

        throw new UnsupportedOperationException();
    }

    <S, U> void cycleBreaker(Key<S> key1, Key<U> key2, BiConsumer<? super S, ? super U> consumer) {
        //
        throw new UnsupportedOperationException();
    }

    // Vi venter med den...
    // Altsaa det er jo kun services den kan exportere...
    // Altsaa vi kan jo have nogle
//    <T, R> ExportedServiceConfiguration<T> export(Factory1<T, R> factory) {
//        // Exports a service by mapping an existing service
//        // Eneste problem er nu har vi exported services som ikke er services...
//        // Men det er vel ikke anderledes end install(X).provide();
//        throw new UnsupportedOperationException();
//    }

    /**
     * Returns an unmodifiable view of the services that are currently available within the container.
     * 
     * @return a unmodifiable view of the services that are currently available within the container
     */
    ServiceRegistry services() {
        // Problemet er vel at vi resolver her...
        throw new UnsupportedOperationException();
    }
    // Skal vi ogsaa supportere noget paa tvaers af containers???
    // Det er vel en slags Wirelet
    // CycleBreaker(SE, ...);
    // CycleBreaker(SE, ...);

    // autoExport

}