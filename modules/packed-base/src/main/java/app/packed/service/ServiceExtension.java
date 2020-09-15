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

import app.packed.base.AttributeProvide;
import app.packed.base.InvalidDeclarationException;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentFactoryDriver;
import app.packed.component.ComponentInstanceDriver;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ComponentLinked;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.hook.OnHook;
import app.packed.inject.Factory;
import app.packed.inject.Provide;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.wirelet.WireletList;
import packed.internal.container.ExtensionAssembly;
import packed.internal.inject.ServiceDependency;
import packed.internal.inject.sidecar.AtProvides;
import packed.internal.inject.sidecar.AtProvidesHook;
import packed.internal.inject.various.ConfigSiteInjectOperations;
import packed.internal.service.InjectionManager;
import packed.internal.service.buildtime.PackedPrototypeConfiguration;
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

    /** The containers injection manager which controls all service functionality. */
    private final InjectionManager im;

    /**
     * Should never be initialized by users.
     * 
     * @param extension
     *            the configuration of the extension
     */
    /* package-private */ ServiceExtension(ExtensionConfiguration extension) {
        this.im = ((ExtensionAssembly) extension).container().im;
    }

    // Skal vi ogsaa supportere noget paa tvaers af bundles???
    // Det er vel en slags Wirelet
    // CycleBreaker(SE, ...);
    // CycleBreaker(SE, ...);

    // Maaske er det her mere injection then service

    protected void addAlias(Class<?> existing, Class<?> newKey) {}

    protected void addAlias(Key<?> existing, Key<?> newKey) {}

    <T> ExportedServiceConfiguration<T> addOptional(Class<T> optional) {
        // @Inject is allowed, but other annotations, types und so weiter is not...
        // Den har ihvertfald slet ikke noget providing...
        throw new UnsupportedOperationException();
    }

//    <S, U> void breakCycle(OP2<S, U> op) {
//        // Denne kraever at vi paa en eller anden maade kan bruge OP2...
//        // MethodHandle op.invoker() <--- Saa maaske er det bare ikke hemmeligt mere.
//        // Eller kan bruge det...
//        throw new UnsupportedOperationException();
//    }

    <T> ExportedServiceConfiguration<T> alias(Class<T> key) {
        // Hmm maaske vi skal kalde den noget andet...
        // SingletonService kan sikkert sagtens extende den...
        // ProtoypeConfiguration has altid en noegle og ikek optional..
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a set of all exported services from this extension. Or null if there are no exports.
     * 
     * @return a set of all exported services from this extension. Or null if there are no exports
     */
    @AttributeProvide(by = ServiceAttributes.class, name = "exported-services")
    @Nullable
    /* package-private */ ServiceMap attributesExports() {
        if (im.hasExports()) {
            return im.exports().exports();
        }
        return null;
    }

    /**
     * Creates a service contract for this extension.
     * 
     * @return a service contract for this extension
     */
    @AttributeProvide(by = ServiceAttributes.class, name = "contract")
    /* package-private */ ServiceContract attributesContract() {
        return im.newServiceContract();
    }

    <S, U> void breakCycle(Key<S> key1, Key<U> key2, BiConsumer<S, U> consumer) {
        // cycleBreaker
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
        // Bundle, Service Extension, ExtensionContext ect...

        // DE her virker kun indefor samme container...

        // Syntes i virkeligheden consumer versionen er bedre....
        // Den er meget mere explicit.
        // Den her skal jo pakke initialisering af U ind i en consumer
    }

    // autoExport

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

    /**
     * Exports a service of the specified type. See {@link #export(Key)} for details.
     * 
     * @param <T>
     *            the type of service to export
     * @param key
     *            the key of the service to export
     * @return a configuration for the exported service
     * @see #export(Key)
     */
    public <T> ExportedServiceConfiguration<T> export(Class<T> key) {
        return export(Key.of(key));
    }

    /**
     * Exports an internal service outside of this bundle.
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
        checkConfigurable();
        return im.exports().export(key, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * 
     */
    // Will never export services that are requirements...

    // One of 3 models...
    // Fails on other exports
    // Ignores other exports
    // interacts with other exports in some way

    public void exportAll() {
        // export all _services_.. Also those that are already exported as something else???
        // I should think not... Det er er en service vel... SelectedAll.keys().export()...
        checkConfigurable();
        im.exports().exportAll(captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    @ComponentLinked(onlyDirectLink = true)
    private void linkChild(ServiceExtension childExtension /* , @WireletSupply Optional<ServiceWireletPipeline> wirelets */) {
        childExtension.configuration();
        // if(configuration.isStronglyAttachedTo(childExtension.configuation())
        im.link(childExtension.im);
    }

    /**
     * Invoked by the runtime for each component using {@link Provide}.
     * 
     * @param hook
     *            the cached hook
     * @param compConf
     *            the configuration of the component that uses the annotation
     */
    @OnHook
    void onHook(AtProvidesHook hook, ComponentNodeConfiguration compConf) {
        if (hook.hasInstanceMembers && compConf.source.isPrototype()) {
            throw new InvalidDeclarationException("Not okay)");
        }
        // Add each @Provide as children of the parent node
        for (AtProvides atProvides : hook.members) {
            im.provideFromAtProvides(compConf, atProvides);
        }
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
        requireNonNull(injector, "injector is null");
        if (!(injector instanceof AbstractInjector)) {
            throw new IllegalArgumentException(
                    "Custom implementations of Injector are currently not supported, injector type = " + injector.getClass().getName());
        }
        checkConfigurable();
        im.provideFromInjector((AbstractInjector) injector, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_PROVIDE_ALL), WireletList.ofAll(wirelets));
    }

    // Will install a ServiceStatelessConfiguration...
    // Spoergmaalet er om vi ikke bare skal have en driver...
    // og en metode paa BaseBundle...
    public <T> PrototypeConfiguration<T> providePrototype(Factory<T> factory) {
        return im.container.compConf.wire(prototype(), factory);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    // javac wants the cast
    public static <T> ComponentFactoryDriver<PrototypeConfiguration<T>, T> prototype() {
        return (ComponentFactoryDriver) ComponentInstanceDriver.of(MethodHandles.lookup(), PackedPrototypeConfiguration.class);
    }

    public void require(Class<?>... keys) {
        checkConfigurable();
        ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE);
        for (Class<?> key : keys) {
            im.dependencies().require(ServiceDependency.of(key), cs);
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
        ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE);
        for (Key<?> key : keys) {
            im.dependencies().require(ServiceDependency.of(key), cs);
        }
    }

    public void requireOptionally(Class<?>... keys) {
        throw new UnsupportedOperationException();
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
        ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE_OPTIONAL);
        for (Key<?> key : keys) {
            im.dependencies().require(ServiceDependency.ofOptional(key), cs);
        }
    }

    /** A subtension useable from other extensions. */
    public final class Sub extends Subtension {

        // Require???
        // export???

        // I don't think extensions should be able to export things.
        // So export annotation should not work on extension services

        /** The other extension type. */
        final Class<? extends Extension> extensionType;

        /**
         * sdsd.
         * 
         * @param extensionType
         *            the type this is a sub extension for
         */
        /* package-private */ Sub(Class<? extends Extension> extensionType) {
            this.extensionType = requireNonNull(extensionType, "extensionType is null");
        }

        // I don't think extensions can export...
        // Except if maybe
//        public void exportd() {
//            export(extensionType);
//        }
    }
}
