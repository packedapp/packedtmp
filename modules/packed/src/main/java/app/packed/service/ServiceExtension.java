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

import java.util.function.Consumer;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.extension.FrameworkExtension;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.BindingProvider.FromOperation;

/**
 * An extension that deals with the service functionality of a container.
 * <p>
 * This extension provides the following functionality:
 * 
 * is extension provides functionality for exposing and consuming services.
 * 
 * 
 */
//Functionality for
//* Explicitly requiring services: require, requiOpt & Manual Requirements Management
//* Exporting services: export, exportAll

//// Was
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

public class ServiceExtension extends FrameworkExtension<ServiceExtension> {

    private final ExtensionSetup setup = ExtensionSetup.crack(this);

    /** Create a new service extension. */
    ServiceExtension() {
    }

    // Validates the outward facing contract
    public void checkContract(Consumer<? super ServiceContract> validator) {
        // Hmm maaske man ville lave et unit test istedet for...
    }

    public void checkContractExact(ServiceContract sc) {
        // Det der er ved validator ServiceContractChecker er at man kan faa lidt mere context med
        // checkContract(Service(c->c.checkExact(sc));// ContractChecker.exact(sc));
    }

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
    // Altsaa tror mere vi er ude efter noget a.la. exported.services = internal.services
    // Saa maske smide ISE hvis der allerede er exporteret services. Det betyder naesten ogsaa
    // at @Export ikke er supporteret
    // ect have exportAll(boolean ignoreExplicitExports) (Otherwise fails)

    // All provided services are automatically exported
    public void exportAll() {
        // Tror vi aendre den til streng service solve...
        // Og saa tager vi bare alle services() og exportere

        // Add exportAll(Predicate); //Maybe some exportAll(Consumer<ExportedConfg>)
        // exportAllAs(Function<?, Key>

        // Export all entries except foo which should be export as Boo
        // exportAll(Predicate) <- takes key or service configuration???

        // export all _services_.. Also those that are already exported as something else???
        // I should think not... Det er er en service vel... SelectedAll.keys().export()...
        checkIsConfigurable();

        setup.container.sm.exportAll = true;
    }

    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            /** {@inheritDoc} */
            @Override
            public void onField(OnField field) {
                Key<?> key = field.fieldToKey();
                boolean constant = field.annotations().readRequired(ProvideService.class).constant();

                OperationSetup operation = OperationSetup.crack(field.newGetOperation(OperationTemplate.defaults()));
                setup.container.sm.serviceProvide(key, constant, operation.bean, operation, new FromOperation(operation));
            }

            /** {@inheritDoc} */
            @Override
            public void onMethod(OnMethod method) {
                Key<?> key = method.methodToKey();
                boolean isProviding = method.annotations().isAnnotationPresent(ProvideService.class);
                boolean isExporting = method.annotations().isAnnotationPresent(ExportService.class);

                OperationTemplate temp = OperationTemplate.defaults().withReturnType(method.operationType().returnType());

                if (isProviding) {
                    boolean constant = method.annotations().readRequired(ProvideService.class).constant();

                    OperationSetup operation = OperationSetup.crack(method.newOperation(temp));
                    setup.container.sm.serviceProvide(key, constant, operation.bean, operation, new FromOperation(operation));
                }

                if (isExporting) {
                    OperationSetup operation = OperationSetup.crack(method.newOperation(temp));
                    setup.container.sm.serviceExport(key, operation);
                }
            }
        };
    }

    /** {@return a mirror for this extension.} */
    @Override
    protected ServiceExtensionMirror newExtensionMirror() {
        return new ServiceExtensionMirror(setup.container);
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
//        if (!(locator instanceof AbstractServiceLocator l)) {
//            throw new IllegalArgumentException("Custom implementations of " + ServiceLocator.class.getSimpleName()
//                    + " are currently not supported, locator type = " + locator.getClass().getName());
//        }
        checkIsConfigurable();
        throw new UnsupportedOperationException();
    }

//    public void provideAll(ServiceLocator locator, Consumer<ServiceTransformer> transformer) {
//        // ST.contract throws UOE
//    }

    // Think we need installPrototype (Which will fail if not provided or exported)
    // providePrototype would then be installPrototype().provide() // not ideal
    // Men taenker vi internt typisk arbejde op i mod implementering. Dog ikke altid
    // providePerRequest <-- every time the service is requested
    // Also these beans, can typically just be composites??? Nah
    public <T> ProvideableBeanConfiguration<T> providePrototype(Class<T> implementation) {
        BeanHandle<T> handle = base().newBean(BeanKind.MANYTON).lifetimes().install(implementation);
        return new ProvideableBeanConfiguration<T>(handle).provide();
    }

    public <T> ProvideableBeanConfiguration<T> providePrototype(Op<T> op) {
        BeanHandle<T> handle = base().newBean(BeanKind.MANYTON).lifetimes().install(op);
        return new ProvideableBeanConfiguration<T>(handle).provide();
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
        checkIsConfigurable();
        // ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE);
        for (@SuppressWarnings("unused")
        Key<?> key : keys) {
            // delegate.ios.requirementsOrCreate().require(key, false /* , cs */);
            throw new UnsupportedOperationException();
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
        checkIsConfigurable();
        // ConfigSite cs = captureStackFrame(ConfigSiteInjectOperations.INJECTOR_REQUIRE_OPTIONAL);
        for (@SuppressWarnings("unused")
        Key<?> key : keys) {
            throw new UnsupportedOperationException();
//            delegate.ios.requirementsOrCreate().require(key, true /* , cs */);
        }
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
    public void transformExports(Consumer<? super Object> transformer) {
        // delegate.ios.exportsOrCreate().setExportTransformer(transformer);
    }
}
