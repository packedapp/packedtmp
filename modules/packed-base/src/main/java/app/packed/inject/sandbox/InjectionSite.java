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
package app.packed.inject.sandbox;

import java.util.Collection;
import java.util.Map;

import app.packed.container.Bundle;

/**
 * A place where Packed can perform injection accordingly to its rules.
 */

// InjectionContext extends InjectionSite?? or context.site?

// Den der context vil vi godt sprede lidt ud taenker jeg...

// Immutable obviously
// Bruger Packed's injection model... Saa niveaut'et over MethodHandle og hvad vi ellers kan finde...

// En single

/// InjectionSite kan ikke sige noget om dynamic services... <- ServiceManager
/// Kun context injections...

//Kan vi spoerge om unresolved dependencies???

// Altsaa vi har 2 sider her... Og maaske 2 different concerns. Men maaske kan vi ikke adskille dem... 
// Det tror jeg ikke vi kan pga annoteringerne og positional... Hvis vi kun havde keys saa ja.
// Men det var jo det Factory doede paa
//

// Dependency???? <--- Parameter... 
//En dependency kender sin context??? 
public interface InjectionSite {

    Map<Integer, Object> positional();// >=0 from the front, <0 from the back

    // Vi ved ikke hvilke extensions der er installeret...

    // Men enten
    // Optional<Extension> extension(); // <--- eller er det paa parameter niveau

    // FunctionDescriptor function();

    /**
     * Finds all injection sites on the specified type
     * 
     * @param componentType
     *            the type to find injection sites on
     * @return a collection of all injection sites on the specified type
     */
    static Collection<InjectionSite> findAll(Class<?> componentType) {
        // Har vi et navn for Collection<InjectionSite>???
        // something extends Iterable<InjectionSite>

        // Altsaa det kan jo liste alle dependencies???+Extensions required

        throw new UnsupportedOperationException();
    }

    static Collection<InjectionSite> findAll(Class<? extends Bundle> bundleType, Class<?> componentType) {
        throw new UnsupportedOperationException();
    }

    static InjectionSite findInitializer(Class<?> componentType) {
        throw new UnsupportedOperationException();
    }

    static InjectionSite findInitializer(Class<? extends Bundle> bundleType, Class<?> componentType) {
        throw new UnsupportedOperationException();
    }

}
// Tager manuelle injection sites med????
// Altsaa dem der bare vil have en Raw MethodHandle???? 
// Taenker kun dem der bruger Packed's model...

// Uhhh hvad med saadan noget som LifecycleState???
// Og andre component/entity specifikke ting???
// Skal vi tage det med som en parameter???
// Altsaa en JPA Entity har jo @Entity...
// Maaske boer vi 100% kunne se det paa typen...

//
// What about build-time...
// En extensionSite <--- extends Extension.. Or is annotated with