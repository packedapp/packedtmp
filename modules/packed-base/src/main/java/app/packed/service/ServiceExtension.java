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

import java.util.function.BiConsumer;

import app.packed.analysis.BundleDescriptor;
import app.packed.base.Key;
import app.packed.base.Key.Qualifier;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.ExtensionLinked;
import app.packed.container.ExtensionSidecar;
import app.packed.container.Wirelet;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;
import app.packed.inject.Factory;
import app.packed.lifecycle.Leaving;
import app.packed.lifecycle2.fn.OP2;
import app.packed.lifecycleold.OnStart;
import app.packed.sidecar.Expose;
import packed.internal.component.PackedSingletonConfiguration;
import packed.internal.container.WireletList;
import packed.internal.inject.ServiceDependency;
import packed.internal.inject.util.InjectConfigSiteOperations;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.service.AtProvidesHook;
import packed.internal.service.buildtime.wirelets.ServiceWireletPipeline;
import packed.internal.service.runtime.AbstractInjector;

/**
 * This extension provides functionality for service management and dependency injection.
 * 
 * 
 */
// Functionality for
// * Explicitly requiring services: require, requiOpt & Manual Requirements Management
// * Exporting services: export, exportAll
// * Providing components or injectors (provideAll)
// * Manual Injection

// Future potential functionality
/// Contracts
/// Security for public injector.... Maaske skal man explicit lave en public injector???
/// Transient requirements Management (automatic require unresolved services from children)
/// Integration pits
// MHT til Manuel Requirements Management
// (Hmm, lugter vi noget profile?? Nahh, folk maa extende BaseBundle og vaelge det..
// Hmm saa auto instantiere vi jo injector extensionen
//// Det man gerne vil kunne sige er at hvis InjectorExtensionen er aktiveret. Saa skal man
// altid bruge Manual Requirements
// contracts bliver installeret direkte paa ContainerConfiguration

// Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
// Maybe have an Bundle.onExtensionActivation(Extension e) <- man kan overskrive....
// Eller @BundleStuff(onActivation = FooActivator.class) -> ForActivator extends BundleController

// Taenker den kun bliver aktiveret hvis vi har en factory med mindste 1 unresolved dependency....
// D.v.s. install(Class c) -> aktivere denne extension, hvis der er unresolved dependencies...
// Ellers selvfoelgelig hvis man bruger provide/@Provides\
public final class ServiceExtension extends Extension {

    /** The service node that does most of the actual work. */
    final ServiceExtensionNode node;

    /** Should never be initialized by users. */
    ServiceExtension(ExtensionConfiguration context) {
        this.node = new ServiceExtensionNode(context);
    }

    // Skal vi ogsaa supportere noget paa tvaers af bundles???
    // Det er vel en slags Wirelet
    // CycleBreaker(SE, ...);
    // CycleBreaker(SE, ...);

    // Maaske er det her mere injection then service
    <S, U> void cycleBreaker(Class<S> keyProducer, Class<U> key2) {
        // keyProducer will have a Consumer<U> injected in its constructor.
        // In which case it must call it exactly once with a valid instance of U.
        // U will then be field/method inject, initialization und so weither as normally.
        // But to the outside it will not that S depends on U.

        // Den der tager en biconsumer supportere ikke at de kan vaere final fields af hinanden...

        // Det kan ogsaa vaere en klasse CycleBreaker.. som tager ContainerConfiguration
        // Bundle, Service Extension, ExtensionContext ect...

        // DE her virker kun indefor samme container...
    }

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

    <S, U> void breakCycle(Key<S> key1, Key<U> key2, BiConsumer<S, U> consumer) {
        // cycleBreaker
        throw new UnsupportedOperationException();
    }

    <S, U> void breakCycle(OP2<S, U> op) {
        // Denne kraever at vi paa en eller anden maade kan bruge OP2...
        // MethodHandle op.invoker() <--- Saa maaske er det bare ikke hemmeligt mere.
        // Eller kan bruge det...
        throw new UnsupportedOperationException();
    }

    <T> ServiceConfiguration<T> addOptional(Class<T> optional) {
        // @Inject is allowed, but other annotations, types und so weiter is not...

        throw new UnsupportedOperationException();
    }

    /**
     * Exports a service of the specified type.
     * 
     * @param <T>
     *            the type of service to export
     * @param key
     *            the key of the service to export
     * @return a configuration for the exported service
     * @see #export(Key)
     */
    public <T> ServiceConfiguration<T> export(Class<T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        return node.exports().export(Key.of(key), captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * Exposes an internal service outside of this bundle.
     * 
     * <pre>
     *  {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class);}
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
     * @param <T>
     *            the type of the service to export
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    public <T> ServiceConfiguration<T> export(Key<T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        return node.exports().export(key, captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * Exports a service represented by the specified service configuration. Is typically used together with
     * {@link #provide(Class)} to export and: <pre>
     * {@code  
     * export(provide(Service.class));
     * }
     * </pre> or <pre>
     * {@code  
     * export(provide(InternalClass.class)).as(ExternalInterface.class);
     * }
     * </pre>
     * 
     * @param <T>
     *            the type of service the configuration creates
     * @param configuration
     *            the service to export
     * @return a new service configuration object representing the exported service
     * @throws IllegalArgumentException
     *             if the specified configuration object was created by another injection extension instance .
     */
    // TODO provide(Foo.class).export instead????

    <T> ServiceConfiguration<T> export(SingletonConfiguration<T> configuration) {
        // Ideen er at man kan ogsaa eksportere en service der overhoved ikke er
        // tilgaengelig internt, men kun externt...

        // export(installInstance("ffffo"));
        throw new UnsupportedOperationException();
    }

    // Hvis man skal eksportere noget under 2 nogler, maa man kalde export 2 gange...
    public <T> ServiceConfiguration<T> export(ServiceComponentConfiguration<T> configuration) {
        requireNonNull(configuration, "configuration is null");
        checkConfigurable();
        return node.exports().export(configuration, captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * 
     */
    // Will never export services that are requirements...
    public void exportAll() {
        checkConfigurable();
        node.exports().exportAll(captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * Requires that all requirements are explicitly added via either {@link #requireOptionally(Key...)},
     * {@link #require(Key...)} or via implementing a contract.
     */
    // Kan vi lave denne generisk paa tvaers af extensions...
    // disableAutomaticRequirements()
    // Jeg taenker lidt det er enten eller. Vi kan ikke goere det per component.
    // Problemet er dem der f.eks. har metoder
    //// Vil det ikke altid bliver efterfuldt af en contract?????
    // Ser ingen grund til baade at support
    // ManualRequirements management..
    // AutoExport with regards to contract???
    public void manualRequirementsManagement() {
        // explicitRequirementsManagement
        checkConfigurable();
        node.dependencies().manualRequirementsManagement();
    }

    /**
     * Invoked by the runtime for each component using {@link Provide}.
     * 
     * @param hook
     *            the cached hook
     * @param cc
     *            the configuration of the component that uses the annotation
     */
    @OnHook
    void onHook(AtProvidesHook hook, SingletonConfiguration<?> cc) {
        node.provider().addProvidesHook(hook, cc);
    }

    @OnHook
    void onHook(AnnotatedMethodHook<Provide> hook, SingletonConfiguration<?> cc) {
        // System.out.println("INVOKED " + hook.method());
    }

    /**
     * @param <T>
     *            the type of service to provide
     * @param implementation
     *            the type of service to provide
     * 
     * @return a configuration of the service
     */
    public <T> ServiceComponentConfiguration<T> provide(Class<T> implementation) {
        return provide(Factory.find(implementation));
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
    public <T> ServiceComponentConfiguration<T> provide(Factory<T> factory) {
        return node.provider().provideFactory((PackedSingletonConfiguration<T>) install(factory));
    }

    // Will install a ServiceStatelessConfiguration...
    <T> ServiceConfiguration<T> provideProtoype(Factory<T> factory) {
        // Hvordan FFF fungere det her???? Vi skal jo vaere knyttet til en component.
        throw new UnsupportedOperationException();
    }

//    public <T> ServiceComponentConfiguration<T> provide(Providable<T> c) {
//        throw new UnsupportedOperationException();
//    }

    <T> ServiceComponentConfiguration<T> provide(SingletonConfiguration<T> c) {
        // return node.provider().provideFactory(install(factory), factory, factory.factory.function);

        // IDeen er lidt at man f.eks. kan lave en ComponentExtension et andet sted, som saa kan bruges her.
        throw new UnsupportedOperationException();
    }

    /**
     * Imports all the services from the specified injector and make each service available to other services in the
     * injector being build.
     * <p>
     * Wirelets can be used to transform and filter the services from the specified injector.
     * 
     * @param injector
     *            the injector to import services from
     * @param wirelets
     *            any wirelets used to filter and transform the provided services
     * @throws IllegalArgumentException
     *             if specifying wirelets that are not defined via {@link ServiceWirelets}
     */
    public void provideAll(Injector injector, Wirelet... wirelets) {
        if (!(requireNonNull(injector, "injector is null") instanceof AbstractInjector)) {
            throw new IllegalArgumentException(
                    "Custom implementations of Injector are currently not supported, injector type = " + injector.getClass().getName());
        }
        checkConfigurable();
        node.provider().provideAll((AbstractInjector) injector, captureStackFrame(InjectConfigSiteOperations.INJECTOR_PROVIDE_ALL), WireletList.of(wirelets));
    }

    /**
     * Binds the specified instance as a new service.
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
    public <T> ServiceComponentConfiguration<T> provideConstant(T instance) {
        // configurability is checked by ComponentExtension
        SingletonConfiguration<T> cc = installInstance(instance);
        return node.provider().provideInstance(cc, instance);
    }

    public void require(Class<?>... keys) {
        checkConfigurable();
        ConfigSite cs = captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE);
        for (Class<?> key : keys) {
            node.dependencies().require(ServiceDependency.of(key), cs);
        }
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
        checkConfigurable();
        ConfigSite cs = captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE);
        for (Key<?> key : keys) {
            node.dependencies().require(ServiceDependency.of(key), cs);
        }
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
    public void requireOptionally(Key<?>... keys) {
        checkConfigurable();
        ConfigSite cs = captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE_OPTIONAL);
        for (Key<?> key : keys) {
            node.dependencies().require(ServiceDependency.ofOptional(key), cs);
        }
    }

    public void requireOptionally(Class<?>... keys) {
        throw new UnsupportedOperationException();
    }

    @Leaving(state = ExtensionSidecar.NORMAL_USAGE)
    void assemble() {
        node.buildBundle();
    }

    /**
     * This method is invoked by the runtime after all children have been configured. But before any guests might have been
     * defined.
     */
    @Leaving(state = ExtensionSidecar.CHILD_LINKING)
    void childenLinked() {
        node.buildTree();
    }

    @Expose
    // Should be Optional<Pipeline>...
    ServiceContract con(ServiceWireletPipeline swp) {
        return node.newServiceContract(swp);
    }

    // Use pipeline???
    @Expose
    void con(BundleDescriptor.Builder builder) {
        node.buildDescriptor(builder);
    }

    @ExtensionLinked(onlyDirectLink = true)
    private void linkChild(ServiceExtension childExtension /* , @WireletSupply Optional<ServiceWireletPipeline> wirelets */) {
        node.link(childExtension.node);
    }
}
