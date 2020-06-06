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
package packed.internal.component.driver;

/**
 *
 *
 * @param <C>
 *            the type of configuration that will be returned to the user
 */
// Noget med scanning....

// Det er jo fucking genialt...
// Det betyder folk kan lave deres egne "component" systemer...

// Vi supporter kun en surragate taenker jeg
// Vi supportere ogsaa kun klasse scanning paa registrering tidspunkt.
// Ikke frit dynamisk taenker jeg. 

// Altsaa er det her maaden at lave prototype service paa???

public abstract class ComponentDriver<C> {

    protected ComponentDriver(Option... options) {

    }

    // Er det ogsaa den her der kommer med til ComponentContext????
    // Ja det tror jeg... Options...

    // ComponentConfigurationContext???
    // Ja med mindre det er saa kompliceret
    protected abstract C create(ComponentDriverContext context);

    // protected abstract C createContainer(ComponentDriverContext context);??

    public static class Option {

        public static Option hosting() {
            throw new UnsupportedOperationException();
        }
        // addHost
        // addGuestType
    }

    // provide(InstanceDriver d)
}

// Vi vil forresten gerne have en SingletonContext der ogsaa fungere for Funktioner med registrere...
// Nej de har jo ikke en type....

// AppHost er simpelthen bare en custom
// singleton component som har host capabilitities.