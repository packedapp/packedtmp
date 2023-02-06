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

import internal.app.packed.container.ContainerKind;
import internal.app.packed.container.PackedContainerLifetimeTemplate;

/**
 *
 */

// Giver ikke mening ikke at have en guest here.
// Det er jo ikke noget man aendrer. Omvendt kan vi ikke goere saa meget ved den

// Maaske installere templaten guest'en
public interface ContainerLifetimeTemplate {

    // Same lifetime as its parent container.

    /**
     * The container will have the same lifetime as its parent container.
     * <p>
     */
    ContainerLifetimeTemplate PARENT = new PackedContainerLifetimeTemplate(ContainerKind.PARENT) ; // no lifetime operations

    ContainerLifetimeTemplate LAZY = new PackedContainerLifetimeTemplate(ContainerKind.LAZY); // no lifetime operations

    ContainerLifetimeTemplate ROOT = new PackedContainerLifetimeTemplate(ContainerKind.ROOT); // no lifetime operations

    // The container exists within the operation that creates it
    // Needs a builder. Because of Context, args
    // Men kan vel godt have statiske context
    ContainerLifetimeTemplate OPERATION = null;

    interface Builder {

        // No seperet MH for starting, part of init
        Builder autoStart(boolean fork);
    }

    /**
     * {@return the
     */
    MethodType invocationType();

    default boolean hasGuest() {
        return mode() == Mode.INITIALIZATION_START_STOP;
    }

    Mode mode();

    public enum Mode {
        INITIALIZATION, // Will initialize
        INITIALIZATION_START_STOP, // Needs a Guest
        FULL_MONTY // May support guests, for example, for a result
    }
}
// Bootstrap <- Initialization only

// App <-- FullMonty

// INITIALIZATION_AND_START_STOP, // Maaske dropper vi simpelthen den her
// Man kan godt kalde 2 method handles...

// Hvis man har initialization kontekst saa bliver de noedt til ogsaa noedt til
// kun at vaere parametere i initialzation. Vi kan ikke sige naa jaa de er ogsaa
// tilgaengelige til start, fordi det er samme lifetime mh. Fordi hvis de nu beslutter
// sig for en anden template for initialization of start er separat... Saa fungere det jo ikke