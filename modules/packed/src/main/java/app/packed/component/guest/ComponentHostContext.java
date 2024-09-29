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
package app.packed.component.guest;

import java.util.Set;

import app.packed.binding.Key;
import app.packed.context.Context;
import app.packed.context.ContextualServiceProvider;
import app.packed.extension.BaseExtension;

/**
 * I think this probably mostly be informational. You would only need it for debugging.
 *
 * @see FromComponentGuest
 * @see OnComponentGuestLifecycle
 */
// Maaske skal vi ikke have denne her...
@ContextualServiceProvider(extension = BaseExtension.class, requiresContext = ComponentHostContext.class)
public interface ComponentHostContext extends Context<BaseExtension> {

    /**
     * {@return services that are available from the guest}
     * <p>
     * This services can be injected into parameter of a factory method using {@link FromComponentGuest}.
     */
    Set<Key<?>> keys();
}

/// Injection af Guest Services
/// Krav: Vi har behov for at kunne se paa guest beanen hvad der kan injectes via mirrors
/// ServiceLocator-> Dynamiske alle services
/// XXX

/// Nuvaerende loesning
// En Host/GuestContext
// En FromComponentGuest annoteringer

// I ny service lookup setup.
// Det er ikke operation, fordi det er en guest bean.
// Det er ikke et namespace fordi det kun er den ene bean det kan injectes i

// Saa Bean
// Eller Context

// Det der taeller for context, er at vi kan faa ComponentHostContext injected.
// Og vi kan se praecis hvad der er.
// Det er svaert med beans, syntes ogsaa det skal vaere forbeholdt en selv...


