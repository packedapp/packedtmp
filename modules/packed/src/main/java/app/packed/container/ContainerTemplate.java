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
package app.packed.container;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.assembly.Assembly;
import app.packed.binding.Key;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import app.packed.operation.Op1;
import app.packed.operation.OperationTemplate;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import sandbox.extension.container.ContainerTemplateLink;
import sandbox.extension.context.ContextSpanKind;

/**
 * A container template must be specified when creating a new container.
 * <p>
 * Lifetime
 *
 * <p>
 *
 *
 * @see app.packed.extension.BaseExtensionPoint#newContainer(ContainerTemplate)
 */
public sealed interface ContainerTemplate permits PackedContainerTemplate {

    /**
     * A base template for a container that has the same lifetime as its parent container.
     * <p>
     * This template does supports any {@link #lifetimeOperations() lifetime operations} as the container is automatically
     * created when the parent container is created.
     * <p>
     * This template does not support carrier objects. (or do we??)
     */
    ContainerTemplate DEFAULT = new PackedContainerTemplate(PackedContainerKind.PARENT_LIFETIME);

    /**
     * A container template representing a container that exists solely within a single entry point operation.
     * <p>
     * The container is created. The method is executed. And the container is shutdown again
     * <p>
     * A container lifetime created using this template must have registered at least one entry point. Otherwise an
     * {@link app.packed.extension.InternalExtensionException} is thrown.
     * <p>
     * TODO we need a method where we can set a supplier that is executed. It is typically a user error. The specified
     * assembly must hava at least one method that schedules shit
     *
     * @see app.packed.extension.BeanElement.BeanMethod#newLifetimeOperation(ContainerHandle)
     * @see app.packed.extension.bean.BeanTemplate#Z_FROM_OPERATION
     **/
    ContainerTemplate GATEWAY = new PackedContainerTemplate(PackedContainerKind.GATEWAY);

    /**
     * A template for a container that is lazily created.
     * <p>
     * The template has no {@link #lifetimeOperations() lifetime operations} as the container is automatically created
     * whenever it is needed by the runtime.
     */
    ContainerTemplate LAZY = new PackedContainerTemplate(PackedContainerKind.LAZY);

    // Cannot have managed on unmanaged
    ContainerTemplate MANAGED = new PackedContainerTemplate(PackedContainerKind.MANAGED);

    // Carefull with Unmanaged on Managed
    ContainerTemplate UNMANAGED = new PackedContainerTemplate(PackedContainerKind.UNMANAGED);

    ContainerTemplate.Descriptor descriptor();

    ContainerTemplate reconfigure(Consumer<? super Configurator> configure);

    public interface Configurator {

        /**
         * Add the specified tags to the application
         *
         * @param tags
         *            the tags to add
         * @return this configurator
         */
        Configurator componentTag(String... tags);

        default <T> Configurator carrierProvideConstant(Class<T> key, T arg) {
            return carrierProvideConstant(Key.of(key), arg);
        }

        /**
         * @see FromLifetimeChannel
         */
        default <T> Configurator carrierProvideConstant(Key<T> key, T arg) {
            throw new UnsupportedOperationException();
        }

        /**
         * Creates a new template that We need to set the holder type. Otherwise we cannot calculate
         * <p>
         * A bean representing the will automatically be created. If you need speciel configuration for the bean. You can
         * manually create one using {@link app.packed.extension.BaseExtensionPoint#containerHolderInstall(Class, boolean)}
         *
         * @param beanClass
         *            the type of the lifetime bean
         * @return the new template
         *
         * @throws UnsupportedOperationException
         *             on container templates that do not have any lifetime operations
         */
        // HOW are we going to access this class????? Without a lookup object.
        // Can't just initialize it
        Configurator carrierType(Class<?> beanClass);

        // Har kun visibility for the installing extension
        Configurator lifetimeOperationAddContext(int index, ContextTemplate template);

        default <T> Configurator localSet(ContainerBuildLocal<T> containerLocal, T value) {
            throw new UnsupportedOperationException();
        }

        Configurator withPack(ContainerTemplateLink pack);

        default Configurator withPack(ContainerTemplateLink... packs) {
            for (ContainerTemplateLink p : packs) {
                withPack(p);
            }
            return this;
        }

        default Configurator zRequireUseOfExtension(String errorMessage) {
            throw new UnsupportedOperationException();
        }
    }

    interface Descriptor {

        /**
         * A set of keys that are available for injection into a lifetime bean using {@link FromLifetimeChannel}.
         * <p>
         * This method is mainly used for informational purposes.
         *
         * @return the set of keys available for injection
         */
        Set<Key<?>> carrierKeys();

        /** {@return a list of the lifetime operation of this container template.} */
        List<OperationTemplate> lifetimeOperations();
    }

    /**
     * A builder for a container (handle).
     *
     * @see BaseExtensionPoint#addCodeGenerated(BeanConfiguration, Class, Supplier)
     * @see BaseExtensionPoint#beanInstallerForExtension(app.packed.extension.bean.BeanTemplate,
     *      app.packed.extension.ExtensionPoint.UseSite)
     */
    sealed interface Installer permits PackedContainerInstaller {

        Installer componentTag(String... tags);

        /**
         * Provides constants per Carrier Instance for this particular container builder
         *
         * @param <T>
         * @param key
         * @param arg
         * @return
         *
         * @see ExtensionLink#ofConstant(Class, Object)
         */
        default <T> Installer carrierProvideConstant(Class<T> key, T constant) {
            return carrierProvideConstant(Key.of(key), constant);
        }

        /**
         * @see FromLifetimeChannel
         */
        <T> Installer carrierProvideConstant(Key<T> key, T constant);

        /**
         *
         * @param holderConfiguration
         * @return
         * @see app.packed.extension.BaseExtensionPoint#installContainerHolder(Class)
         * @throws IllegalArgumentException
         *             if the holder class of the bean does not match the holder type set when creating the container template.
         */
        // LifetimeCarrier?
//        default Installer carrierUse(ComponentGuestAdaptorBeanConfiguration<?> holderConfiguration) {
//            // Gaar udfra vi maa definere wrapper beanen alene...Eller som minimum
//            // supportere det
//            // Hvis vi vil dele den...
//
//            // Det betyder ogsaa vi skal lave en wrapper bean alene
//            return this;
//        }

        // Only Managed-Operation does not require a wrapper
        // For now this method is here. Might move it to the actual CHC at some point

        // Hmm, don't know if need a carrier instance, if we have implicit construction
        // /**
        // * @return
        // * @throws UnsupportedOperationException
        // * if a carrier type was not defined in the container template
        // */
        // default ContainerCarrierConfiguration<?> carrierInstance() {
        // throw new UnsupportedOperationException();
        // }

        /**
         * Creates a new container using the specified assembly.
         * <p>
         * The container handle returned by this method is no longer {@link ContainerHandle#isConfigurable() configurable}.
         * Configuration of the new container must be done prior to calling this method.
         *
         * @param assembly
         *            the assembly to link
         * @param wirelets
         *            optional wirelets
         * @return a container handle representing the new container
         *
         * @see #build(Wirelet...)
         */
        <H extends ContainerHandle<?>> H install(Assembly assembly, Function<? super ContainerTemplate.Installer, H> factory, Wirelet... wirelets);

        /**
         * Creates a new configurable container.
         *
         * @param wirelets
         *            optional wirelets
         * @return a container handle representing the new container
         *
         * @see #install(Assembly, Wirelet...)
         */
        <H extends ContainerHandle<?>> H install(Function<? super ContainerTemplate.Installer, H> factory, Wirelet... wirelets);

        /**
         * Creates the new container and adds this extension to the new container.
         * <p>
         * The extension in new the container can be obtained by calling {@link Extension#fromHandle(ContainerHandle)}
         *
         * @return a container handle representing the new container
         *
         * @see app.packed.extension.Extension#fromHandle(ContainerHandle)
         */
        <H extends ContainerHandle<?>> H installAndUseThisExtension(Function<? super ContainerTemplate.Installer, H> factory, Wirelet... wirelets);

        /**
         * Sets the value of the specified container local for the container being built.
         *
         * @param <T>
         *            the type of value the container local holds
         * @param local
         *            the container local to set
         * @param value
         *            the value of the local
         * @return this builder
         */
        // Do we allow non-container scope??? I don't think so
        // initializeLocalWith??
        <T> Installer setLocal(ContainerBuildLocal<T> containerLocal, T value);

        /**
         * <p>
         * TODO: How do we handle conflicts? I don't think we should fail
         * <p>
         * TODO This is probably overridable by Wirelet.named()
         * <p>
         * Beans not-capitalized? Containers capitalized
         *
         * @param name
         *            the name of the container
         * @return this builder
         */
        Installer named(String name);

    }

}

interface Zandbox {

    // @ContextProvide(Context.class) T, hvor T=ArgType
    Zandbox addContextFromArg(ContextTemplate template);

    @SuppressWarnings("unchecked")
    // We have a trivial usecases where the bean is the same parameter
    // Take a record? that matches the parameters?
    <T> Zandbox addContextFromParent(ContextTemplate template, ContextSpanKind span, Class<?> extensionBean, Op1<T, ?>... op);

    // Soeger vi kun i samme lifetime?
    Zandbox addContextFromProvide(ContextTemplate template, ContextSpanKind containerSpan);

    // BeanSpan not supported
    // OperationSpan I will have to think about that

    default ContainerTemplate allowRuntimeWirelets() {
        throw new UnsupportedOperationException();
    }
    // context either from args which are then stored
    // or from some kind of ContextProvide method

    // I don't know what these do
    // In order to add links. This must have been set
    // Other we do not
    // Contexts?
    Optional<Class<? extends Extension<?>>> installedBy();

    ContainerTemplate installedBy(Class<? extends Extension<?>> installedBy);
}
//Hvis man har initialization kontekst saa bliver de noedt til ogsaa noedt til
//kun at vaere parametere i initialzation. Vi kan ikke sige naa jaa de er ogsaa
//tilgaengelige til start, fordi det er samme lifetime mh. Fordi hvis de nu beslutter
//sig for en anden template for initialization of start er separat... Saa fungere det jo ikke

//interface ZBuilder2 {
//
//    <T> ZBuilder2 guest(Class<T> guest, Consumer<? super InstanceBeanConfiguration<T>> onInstaller);
//
//    <T> ZBuilder2 guest(Op<T> guest, Consumer<? super InstanceBeanConfiguration<T>> onInstaller);
//}

// Features
////Kan expose services that can be used together ContainerGuest
////Kan

////Args
////Contexts???

//Ideen er at installere beans der kan be exposed to guest objektet.
//Fx ServiceLocator
//Hvorfor er det ikke bare extensions der installere den og ikke bridgen

//(e-> installGuest(F.class).dasd);

//OperationTemplate???

//123 paa runtime som argument.
//Hvordan faar jeg det ind i en bean
//Anden end via ContextInjection???

//InvocationContextArgument

//Create an internalContext???

//Bliver noedt til at vaere unik. Kan ikke add

//
//public Builder<E> provide(Class<?> extensionBean, Op<?> op) {
//// Adds synthetic operation to extensionBean
//return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Class<T> key) {
//return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Key<T> key) {
//return this;
//}
