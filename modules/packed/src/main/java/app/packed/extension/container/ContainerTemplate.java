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
package app.packed.extension.container;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.context.ContextSpan;
import app.packed.extension.Extension;
import app.packed.extension.context.ContextTemplate;
import app.packed.extension.operation.OperationTemplate;
import app.packed.operation.Op1;
import app.packed.util.Key;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;

/**
 * A container template is
 *
 * Con
 *
 * @see BaseExtensionPoint#containerInstaller(ContainerTemplate)
 */
public sealed interface ContainerTemplate permits PackedContainerTemplate {

    /**
     * A template for a container that has the same lifetime as its parent container.
     * <p>
     * The template has no {@link #lifetimeOperations() lifetime operations} as the container is automatically created when
     * the root container in the lifetime is created.
     */
    ContainerTemplate DEFAULT = new PackedContainerTemplate(PackedContainerKind.PARENT, void.class, List.of(), null);

    /**
     * A template for a container that is lazily created.
     * <p>
     * The template has no {@link #lifetimeOperations() lifetime operations} as the container is automatically created
     * whenever it is needed by the runtime.
     */
    // Kan man have lazy paa unmanaged????
    ContainerTemplate LAZY = new PackedContainerTemplate(PackedContainerKind.LAZY, void.class, List.of(), null);

    // Cannot have managed on unmanaged
    ContainerTemplate MANAGED = null;

    ContainerTemplate MANAGED_AUTOSTART = null;

    // Carefull with Unmanaged on Managed
    ContainerTemplate UNMANAGED = null;

    /**
     * A container template representing a container that exists solely within a single operation.
     * <p>
     * The container is created. The method is executed. And the container is shutdown again
     * <p>
     * A container created using this template must have registered at least one spawning operation. Otherwise an
     * {@link app.packed.extension.InternalExtensionException} is thrown. TODO we need a method where we can set a supplier
     * that is executed. It is typically a user error. The specified assembly must hava at least one method that schedules
     * shit
     *
     *
     * @see app.packed.extension.BeanElement.BeanMethod#newLifetimeOperation(ContainerHandle)
     * @see app.packed.extension.bean.BeanTemplate#Z_FROM_OPERATION
     **/
    ContainerTemplate FROM_OPERATIONS = null;

    ContainerTemplate addLink(ExtensionLink link);

    default ContainerTemplate addLink(ExtensionLink... links) {
        for (ExtensionLink l : links) {
            addLink(l);
        }
        return this;
    }

    default ContainerTemplate allowRuntimeWirelets() {
        throw new UnsupportedOperationException();
    }

    /**
     * A set of keys that are available for injection into the holder using {@link FromLifetimeChannel}.
     * <p>
     * This method is mainly used for informational purposes.
     *
     * @return the set of keys available for injection
     *
     * @see From
     */
    Set<Key<?>> holderKeys();

    /**
     * Creates a new template that We need to set the holder type. Otherwise we cannot calculate
     * <p>
     * A bean representing the will automatically be created. If you need speciel configuration for the bean. You can
     * manually create one using {@link app.packed.extension.BaseExtensionPoint#containerHolderInstall(Class, boolean)}
     *
     * @param guest
     * @return the new template
     *
     * @throws UnsupportedOperationException
     *             on container templates that do not have any lifetime operations
     */
    ContainerTemplate holder(Class<?> guest);

    default <T> ContainerTemplate holderProvideConstant(Class<T> key, T arg) {
        return holderProvideConstant(Key.of(key), arg);
    }

    /**
     * @see FromLifetimeChannel
     */
    default <T> ContainerTemplate holderProvideConstant(Key<T> key, T arg) {
        throw new UnsupportedOperationException();
    }

    // Har kun visibility for the installing extension
    ContainerTemplate lifetimeOperationAddContext(int index, ContextTemplate template);

    /** {@return a list of the lifetime operation templates for this template.} */
    List<OperationTemplate> lifetimeOperations();
}

interface Zandbox {

    // context either from args which are then stored
    // or from some kind of ContextProvide method

    // @ContextProvide(Context.class) T, hvor T=ArgType
    Zandbox addContextFromArg(ContextTemplate template);

    @SuppressWarnings("unchecked")
    // We have a trivial usecases where the bean is the same parameter
    // Take a record? that matches the parameters?
    <T> Zandbox addContextFromParent(ContextTemplate template, ContextSpan span, Class<?> extensionBean, Op1<T, ?>... op);

    // BeanSpan not supported
    // OperationSpan I will have to think about that

    // Soeger vi kun i samme lifetime?
    Zandbox addContextFromProvide(ContextTemplate template, ContextSpan containerSpan);

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
