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

import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.bindings.Key;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
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
public sealed interface ContainerLifetimeTemplate extends OperationTemplate permits PackedContainerLifetimeTemplate {

    ContainerLifetimeTemplate LAZY = new PackedContainerLifetimeTemplate(ContainerKind.LAZY);

    // The container exists within the operation that creates it
    // Needs a builder. Because of Context, args
    // Men kan vel godt have statiske context
    ContainerLifetimeTemplate OPERATION = null;

    /**
     * The container will have the same lifetime as its parent container.
     * <p>
     */
    ContainerLifetimeTemplate PARENT = new PackedContainerLifetimeTemplate(ContainerKind.PARENT);

    // services available
    /**
     * @return
     *
     * @see From
     */
    default Set<Key<?>> keys(){
        return Set.of();
    }

    default Class<?> guestClass() {
        return void.class;
    }


    // Parent, Lazy har ikke en invocation type.
    // Vi har aldrig mere end en. Da man lukker via at faa noget injected i en guest.
    /**
     * {@return the
     */
    default Optional<MethodType> invocationType() {
        throw new UnsupportedOperationException();
    }

    Mode mode();

    static Builder builder() {
        throw new UnsupportedOperationException();
    }

    interface Builder {

        Builder addBridge(ExtensionLifetimeBridge bridge);

        Builder guest(Class<?> guest);

        Builder guest(Op<?> guest);

        // No seperet MH for starting, part of init
        Builder autoStart(boolean fork);

        ContainerLifetimeTemplate build();
    }

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