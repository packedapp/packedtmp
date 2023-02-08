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
package app.packed.lifetime;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.bindings.Key;
import app.packed.context.ContextSpan;
import app.packed.context.ContextTemplate;
import app.packed.operation.Op;
import app.packed.operation.Op1;
import internal.app.packed.container.ContainerKind;
import internal.app.packed.container.PackedContainerLifetimeTemplate;

/**
 *
 */
// Features
//// Guest
//// Extension Bridges
//// Args
//// Contexts
public sealed interface ContainerLifetimeTemplate extends LifetimeTemplate permits PackedContainerLifetimeTemplate {

    // HostClass, ManagedHostBean
    ContainerLifetimeTemplate HOSTED = null;

    // The container exists within the operation that creates it
    // Needs a builder. Because of Context, args
    // Men kan vel godt have statiske context

    // Er jo en slags new lifetime. Man kunne jo godt sige koer den her operation
    ContainerLifetimeTemplate LAZY = new PackedContainerLifetimeTemplate(ContainerKind.LAZY);

    // Har vel FullLifetime, HalfLifetime
    ContainerLifetimeTemplate OPERATION = null;

    /**
     * The container will have the same lifetime as its parent container.
     * <p>
     */
    ContainerLifetimeTemplate PARENT = new PackedContainerLifetimeTemplate(ContainerKind.PARENT);

    default Class<?> guestClass() {
        return void.class;
    }

    /**
     * The set of keys that are available for injection into a host using {@link FromLifetimeChannel}.
     *
     * @return the set of keys available for injection
     *
     * @see From
     */
    default Set<Key<?>> keys() {
        return Set.of();
    }

    Mode mode();

    default <T> ContainerLifetimeChannel withConstant(Class<T> key, T arg) {
        return withConstant(Key.of(key), arg);
    }

    default <T> ContainerLifetimeChannel withConstant(Key<T> key, T arg) {
        throw new UnsupportedOperationException();
    }

    // Maybe skip the builder and just have with'ers
    static Builder builder() {
        throw new UnsupportedOperationException();
    }

    interface Builder {

        Builder addChannel(ContainerLifetimeChannel channel);

        // BeanSpan not supported
        // OperationSpan I will have to think about that

        // LifetimeOperation
        // What about PARENT thing
        // I can't see how this can work without it being extract from some beans???
        // a.la provideContext(ContextClass, index)
        // Nej maa vaere en form for Container Context pan
        Builder addContext(ContextTemplate template, ContextSpan containerSpan);

        @SuppressWarnings("unchecked")
        // We have a trivial usecases where the bean is the same parameter
        // Take a record? that matches the parameters?
        <T> Builder addContextInParent(ContextTemplate template, ContextSpan span, Class<?> extensionBean, Op1<T, ?>... op);

        Builder allowRuntimeWirelets();

        // No seperet MH for starting, part of init
        Builder autoStart(boolean forkOnStart);

        /**
         * Creates a returns the new lifetime template.
         *
         * @return the new lifetime template
         */
        ContainerLifetimeTemplate build();

        /**
         * @param guest
         * @return
         * @throws IllegalStateException
         *             if a host bean has already been registered
         */
        // Hvis der allerede eksistere en guest med den exact type saa bruger vi den
        // Ellers creater vi den lazily

        // Tror det er den letteste maade at supportere configuration af beanen
        // hvis man har behov for det
        Builder hostAs(Class<?> guest);
    }

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
// // Adds synthetic operation to extensionBean
// return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Class<T> key) {
// return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Key<T> key) {
// return this;
//}

//public <T> Builder<E> provide(Key<T> key, Class<T> type) {
// bridge = bridge.addInvocationArgument(type);
// return this;
//}
//
//public <T> Builder<E> provide(Class<T> type, Key<?> ) {
// bridge = bridge.addInvocationArgument(type);
// return this;
//}
    public enum Mode {
        FULL_MONTY, // May support guests, for example, for a result, // Will initialize
        INITIALIZATION, // Needs a Guest
        INITIALIZATION_START_STOP
    }
}

interface ZBuilder2 {

    <T> ZBuilder2 guest(Class<T> guest, Consumer<? super InstanceBeanConfiguration<T>> onInstaller);

    <T> ZBuilder2 guest(Op<T> guest, Consumer<? super InstanceBeanConfiguration<T>> onInstaller);

}
// Bootstrap <- Initialization only

// App <-- FullMonty

// INITIALIZATION_AND_START_STOP, // Maaske dropper vi simpelthen den her
// Man kan godt kalde 2 method handles...

// Hvis man har initialization kontekst saa bliver de noedt til ogsaa noedt til
// kun at vaere parametere i initialzation. Vi kan ikke sige naa jaa de er ogsaa
// tilgaengelige til start, fordi det er samme lifetime mh. Fordi hvis de nu beslutter
// sig for en anden template for initialization of start er separat... Saa fungere det jo ikke