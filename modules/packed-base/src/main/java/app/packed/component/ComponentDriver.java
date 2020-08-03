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
package app.packed.component;

/**
 * 
 * 
 * @param <C>
 *            the type of configuration the driver expose to users for configuring the underlying component
 * 
 * @apiNote In the future, if the Java language permits, {@link ComponentDriver} may become a {@code sealed} interface,
 *          which would prohibit subclassing except by explicitly permitted types.
 */
public interface ComponentDriver<C> {

    // Har man altid en source???????

    default C newConfiguration() {
        throw new UnsupportedOperationException();
    }
}

// COMPONENT_DRIVEr definere ingen drivere selv..
// Skal den vaere here eller paa ComponentDriver????
// Syntes egentlig ikke den er tilknyttet ComponentDriver...
// Men hvis folk selv definere for custom defineret vil det maaske give mening.
// At smide dem paa configurationen... Der er jo ingen

// En anden meget positiv ting er at vi vi har 2 component drivere
// sourced and unsourced. People shouldn't really need to look at
// both of the classes two find what they need..

//interface InstanDriver<T, C>

interface ComponentDriverContext {

}

/**
 *
 *
 * @param <C>
 *            the type of configuration that will be returned to the user
 */
//Noget med scanning....

//Det er jo fucking genialt...
//Det betyder folk kan lave deres egne "component" systemer...

//Vi supporter kun en surragate taenker jeg
//Vi supportere ogsaa kun klasse scanning paa registrering tidspunkt.
//Ikke frit dynamisk taenker jeg. 

//Altsaa er det her maaden at lave prototype service paa???

//Vi vil forresten gerne have en SingletonContext der ogsaa fungere for Funktioner med registrere...
//Nej de har jo ikke en type....

abstract class OldComponentDriver<C> {

    protected OldComponentDriver(Option... options) {

    }

    // Er det ogsaa den her der kommer med til ComponentContext????
    // Ja det tror jeg... Options...

    // ComponentConfigurationContext???
    // Ja med mindre det er saa kompliceret
    protected abstract C create(ComponentDriverContext context);

    static class Option {

        static Option hosting() {
            throw new UnsupportedOperationException();
        }
        // addHost
        // addGuestType
    }

    // provide(InstanceDriver d)
}
