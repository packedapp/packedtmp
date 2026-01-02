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

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.assembly.Assembly;
import app.packed.binding.Key;
import app.packed.binding.Provider;
import app.packed.component.ComponentRealm;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceConfiguration;
import app.packed.operation.OperationConfiguration;
import app.packed.service.bridge.old.ServiceOutgoingTransformer;
import internal.app.packed.invoke.MethodHandleUtil;
import internal.app.packed.service.ServiceNamespaceHandle;
import internal.app.packed.service.util.PackedServiceLocator;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.ServiceAccessHandler;

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
    ServiceNamespaceConfiguration(ServiceNamespaceHandle handle, BaseExtension extension, ComponentRealm actor) {
        super(handle, extension, actor);
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
        Map<Key<?>, ?> result;
        if (locator instanceof PackedServiceLocator psl) {
//            result = CollectionUtil.copyOf(psl.entries(), e -> e.bindTo(psl.context()));
            return psl.keys();
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

    static {
        AccessHelper.initHandler(ServiceAccessHandler.class, new ServiceAccessHandler() {

            @Override
            public ServiceNamespaceConfiguration newServiceNamespaceConfiguration(ServiceNamespaceHandle handle, BaseExtension extension) {
                return new ServiceNamespaceConfiguration(handle, extension, ComponentRealm.userland());
            }

            @Override
            public ServiceNamespaceMirror newServiceNamespaceMirror(ServiceNamespaceHandle handle) {
                return new ServiceNamespaceMirror(handle);
            }
        });
    }
}


//Functionality for
//* Explicitly requiring services: require, requiOpt & Manual Requirements Management
//* Exporting services: export, exportAll

////Was
//Functionality for
//* Explicitly requiring services: require, requiOpt & Manual Requirements Management
//* Exporting services: export, exportAll
//* Providing components or injectors (provideAll)
//* Manual Injection

//Har ikke behov for delete fra ServiceLocator

//Future potential functionality
/// Contracts
/// Security for public injector.... Maaske skal man explicit lave en public injector???
/// Transient requirements Management (automatic require unresolved services from children)
/// Integration pits
//MHT til Manuel Requirements Management
//(Hmm, lugter vi noget profile?? Nahh, folk maa extende BaseAssembly og vaelge det..
//Hmm saa auto instantiere vi jo injector extensionen
////Det man gerne vil kunne sige er at hvis InjectorExtensionen er aktiveret. Saa skal man
//altid bruge Manual Requirements
//contracts bliver installeret direkte paa ContainerConfiguration

//Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
//Maybe have an Container.onExtensionActivation(Extension e) <- man kan overskrive....
//Eller @ContainerStuff(onActivation = FooActivator.class) -> ForActivator extends ContainerController

//Taenker den kun bliver aktiveret hvis vi har en factory med mindste 1 unresolved dependency....
//D.v.s. install(Class c) -> aktivere denne extension, hvis der er unresolved dependencies...
//Ellers selvfoelgelig hvis man bruger provide/@Provides\
//final void embed(Assembly assembly) {
///// MHT til hooks. Saa tror jeg faktisk at man tager de bean hooks
////der er paa den assembly der definere dem
//
////Men der er helt klart noget arbejde der
//throw new UnsupportedOperationException();
//}

class ZServiceSandbox {

    public void applicationLink(Assembly assembly, Wirelet... wirelets) {
        // Syntes den er maerkelig hvis vi havde ApplicationWirelet.NEW_APPLICATION
        // Den her er klarere
        // linkNewContainerBuilder().build(assembly, wirelets);
    }

    // Validates the outward facing contract
    public void checkContract(Consumer<? super ServiceContract> validator) {}

    // Det der er ved validator ServiceContractChecker er at man kan faa lidt mere context med
    // checkContract(Service(c->c.checkExact(sc));// ContractChecker.exact(sc));
    public void checkContractExact(ServiceContract sc) {}

    public void exportsTransform(Consumer<? super ServiceOutgoingTransformer> s) {

    }

    public void provideAll(ServiceLocator locator, Consumer<Object> transformer) {
        // ST.contract throws UOE
    }

    public void resolveFirst(Key<?> key) {}

}