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
package app.packed.lifecyclenext;

import static java.util.Objects.requireNonNull;

import java.util.Set;

/**
 * Any entity that has a lifecycle can have a lifecycle context object injected. This object contains information about
 * the actual and the desired state. The two may differ:
 * 
 * <p>
 * <ul>
 * <li>Actual State: The actual state of the entity
 * <li>Desired State: The desired state of the entity.
 * <li>the third item
 * </ul>
 * <p>
 * 
 * 
 */
// All objects always have linier progressional state
// For example, even though Packed supports restarting. New entities are always created that starts out in the same
// state as the failed entities started out in.
// lifecycle state and concurrencxy.. 
//I mean it is possible to set the styate of a component for elsewhere...
// So I think you would assume people should be able to query it outside of context
// Syntes bare det er irriterende at skulle opdatere en volatile variable 10 gange...
// Men det skal vi jo heller ikke... eftersom vi er single threaded...

// Maaske er alle extension lifeycle ens...
// Nej fordi nogle extensions skal have lov til at kalde 
// visse metoder i f.eks. onConfigured. Som man kun kan kalde paa
// extensions som ikke er faerdigkonfigureret.... -> Vi har brug for en state per extension...
// Saa maaske bare smide en lifecycle state en dre wrapper ExtensionSidecar...

// Men f.eks. for ExtensionSidecar saa er staten jo ens for alle extensions taenker jeg...
// Eller arghh hvis der er en extension der fejler...

public interface FutureLifecycleContext {

    String actualState();

    /**
     * Checks that the actual state is the specified expected state or throws an {@link AssertionError}.
     * 
     * @param expectedState
     *            the expected state
     * @throws AssertionError
     *             if the actual state is not equivalent to specified expected state
     * @see #actualState()
     */
    default void assertState(String expectedState) {
        requireNonNull(expectedState, "expectedState is null");
        String actual = actualState();
        if (!expectedState.equals(actual)) {
            throw new AssertionError("Expected state '" + expectedState + "' but was '" + actual + "'");
        }
    }

    default void checkState(String expectedState) {
        requireNonNull(expectedState, "expectedState is null");
        String actual = actualState();
        if (!expectedState.equals(actual)) {
            throw new IllegalStateException("Expected state '" + expectedState + "' but was '" + actual + "'");
        }
    }

    String desiredState();

    Set<String> stateset();

    // actual state
    // desired state
    //

    EntityType type(); // Also on InjectionContext...
}
// allStates
// currentState
// possible future states
// State Graph
// Transitions can either be normal or exceptional
// History.. All, latest states

// NamedState
// interface LifecycleState {String name, boolean isCustom, Set<String> from, Set<String> to} 

// @LifecycleOptions(maintainDetailedHistory = true, aliases = "doo=foo")
// @InjectionOptions()7

// OnInstantiation... -> works best if Packed instantiates the object... Otherwise it sounds stupid...

/// Also a generic

// State graph...
/// ExecutionGraph

////////////// ----------------
// LifecycleContext <---- Only for lifecycle methods?????
// Nej for alle vel...

//Kan man injectes uden at have en lifecycle??? Ja taenker f.eks. constants....

//Maaske har vi en InjectionContext.contextTypes() ??????

//Maaske har vi et speciel Context interface...

// Altsaa Contexts
// InjectionContext -> For objekter der kan injectes...
// LifecycleContext -> For objekter der har en lifecycle (Provided by the runtime....)
// ContainerContext -> Et shared objekt blandt alle i samme container??? Hvad skal det kunne

// ComponentContext <- Kan get alle context for every method?? F.eks. det SchedulingContext of
// Method x???

// Context are threadsafe unless otherwise specified...

// Kan vi angive hvilke entity typer en annotering kan bruges paaa???
/// F.eks. OnStart kan bruges paa Components, og MethodSidecar...