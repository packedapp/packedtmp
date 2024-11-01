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

import java.util.Optional;

import app.packed.binding.Key;
import app.packed.component.guest.OldContainerTemplateLink;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import app.packed.operation.Op1;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
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
// Managed/Unmanaged
// Root/NoRoot
// Linked/Configuration
// ParentLifetime/NewLifetime
public sealed interface ContainerTemplate<H extends ContainerHandle<?>> permits PackedContainerTemplate {

    /**
     * A base template for a container that has the same lifetime as its parent container.
     * <p>
     * This template does supports any {@link #lifetimeOperations() lifetime operations} as the container is automatically
     * created when the parent container is created.
     * <p>
     * This template does not support carrier objects. (or do we??)
     */
    ContainerTemplate<?> DEFAULT = new PackedContainerTemplate<>(PackedContainerKind.FROM_CONTAINER);

    // Optional, to support #DEFAULT
    boolean isManaged();

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
    ContainerTemplate<?> GATEWAY = new PackedContainerTemplate<>(PackedContainerKind.GATEWAY);

    // Cannot have managed on unmanaged
    ContainerTemplate<?> MANAGED = new PackedContainerTemplate<>(PackedContainerKind.MANAGED);

//    /**
//     * A template for a container that is lazily created.
//     * <p>
//     * The template has no {@link #lifetimeOperations() lifetime operations} as the container is automatically created
//     * whenever it is needed by the runtime.
//     */
//    ContainerTemplate LAZY = new PackedContainerTemplate(PackedContainerKind.LAZY);

    // Carefull with Unmanaged on Managed
    ContainerTemplate<?> UNMANAGED = new PackedContainerTemplate<>(PackedContainerKind.UNMANAGED);

    /**
     * @param <T>
     * @param key
     * @param arg
     * @return
     * @see app.packed.component.guest.FromComponentGuest
     */
    default <T> ContainerTemplate<H> withCarrierProvideConstant(Class<T> key, T arg) {
        return withCarrierProvideConstant(Key.of(key), arg);
    }

    /**
     * @see FromLifetimeChannel
     */
    default <T> ContainerTemplate<H> withCarrierProvideConstant(Key<T> key, T arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Add the specified tags to the application
     *
     * @param tags
     *            the tags to add
     * @return this ContainerTemplate<H>
     */
    ContainerTemplate<H> withComponentTag(String... tags);

//        // Maybe put it on ContainerTemplate.
//        default ContainerTemplate<H> configureRootContainerHandleFactory(Function<? super ContainerTemplate.Installer<?>, ? extends ContainerHandle<?>> handleFactory) {
//            throw new UnsupportedOperationException();
//        }

//        /**
//         * Creates a new template that We need to set the holder type. Otherwise we cannot calculate
//         * <p>
//         * A bean representing the will automatically be created. If you need speciel configuration for the bean. You can
//         * manually create one using {@link app.packed.extension.BaseExtensionPoint#containerHolderInstall(Class, boolean)}
//         *
//         * @param beanClass
//         *            the type of the lifetime bean
//         * @return the new template
//         *
//         * @throws UnsupportedOperationException
//         *             on container templates that do not have any lifetime operations
//         */
//        // HOW are we going to access this class????? Without a lookup object.
//        // Can't just initialize it'
//        @Deprecated // Guest bean
//        ContainerTemplate<H> carrierType(Class<?> beanClass);

    // Har kun visibility for the installing extension
    ContainerTemplate<H> withLifetimeOperationAddContext(int index, ContextTemplate template);

    default <T> ContainerTemplate<H> withLocal(ContainerBuildLocal<T> containerLocal, T value) {
        throw new UnsupportedOperationException();
    }

    ContainerTemplate<H> withPack(OldContainerTemplateLink pack);

    default ContainerTemplate<H> withPack(OldContainerTemplateLink... packs) {
        for (OldContainerTemplateLink p : packs) {
            withPack(p);
        }
        return this;
    }

}

interface Zandbox {

    default Zandbox zRequireUseOfExtension(String errorMessage) {
        throw new UnsupportedOperationException();
    }

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

    default ContainerTemplate<?> allowRuntimeWirelets() {
        throw new UnsupportedOperationException();
    }
    // context either from args which are then stored
    // or from some kind of ContextProvide method

    // I don't know what these do
    // In order to add links. This must have been set
    // Other we do not
    // Contexts?
    Optional<Class<? extends Extension<?>>> installedBy();

    ContainerTemplate<?> installedBy(Class<? extends Extension<?>> installedBy);
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
