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
import internal.app.packed.container.ContainerKind;
import internal.app.packed.container.PackedContainerTemplate;

/**
 * A container template is
 *
 *
 * @see BaseExtensionPoint#containerInstaller(ContainerTemplate)
 */
public sealed interface ContainerTemplate permits PackedContainerTemplate {

    ContainerTemplate UNMANAGED = null;

    ContainerTemplate MANAGED = null;

    /**
     * A template for a container that has the same lifetime as its parent container.
     * <p>
     * This template
     */
    ContainerTemplate IN_PARENT = new PackedContainerTemplate(ContainerKind.PARENT, void.class);

    /**
     * A template for a container that is lazily created.
     * <p>
     * This template
     */
    ContainerTemplate LAZY = new PackedContainerTemplate(ContainerKind.LAZY, void.class);

    // The container exists within the operation that creates it
    // Needs a builder. Because of Context, args
    // Men kan vel godt have statiske context
    // Har vel FullLifetime, (HalfLifetime?)
    ContainerTemplate OPERATION = null;

    // lazyCreateHolder
    ContainerTemplate holder(Class<?> guest);

    Class<?> holderClass();

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

    default <T> Builder holderProvideConstant(Class<T> key, T arg) {
        return holderProvideConstant(Key.of(key), arg);
    }

    /**
     * @see FromLifetimeChannel
     */
    default <T> Builder holderProvideConstant(Key<T> key, T arg) {
        throw new UnsupportedOperationException();
    }

    // In order to add links. This must have been set
    // Other we do not
    Optional<Class<? extends Extension<?>>> installedBy();

    ContainerTemplate installedBy(Class<? extends Extension<?>> installedBy);


    /** {@return a list of the various lifetime operations of this template.} */
    List<OperationTemplate> operations();

    ContainerTemplate linkWith(ExtensionLink channel);

    interface Builder {

        // context either from args which are then stored
        // or from some kind of ContextProvide method

        // @ContextProvide(Context.class) T, hvor T=ArgType
        Builder addContextFromArg(ContextTemplate template, ContextSpan containerSpan);

        // Soeger vi kun i samme lifetime?
        Builder addContextFromProvide(ContextTemplate template, ContextSpan containerSpan);

        // BeanSpan not supported
        // OperationSpan I will have to think about that

        @SuppressWarnings("unchecked")
        // We have a trivial usecases where the bean is the same parameter
        // Take a record? that matches the parameters?
        <T> Builder addContextFromParent(ContextTemplate template, ContextSpan span, Class<?> extensionBean, Op1<T, ?>... op);

        Builder allowRuntimeWirelets();

        // No seperet MH for starting, part of init
        Builder autoStart(boolean forkOnStart);
    }
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
