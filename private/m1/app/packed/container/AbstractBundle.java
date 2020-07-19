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
package app.packed.container;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */

// Ideen er lidt at vi kan lave en ActorBundle
// Hvor man ikke kan eksportere services
// og alt muligt andet...
// Saa den er helt clean...

// Skal have en version der ikke eksportere nogle som helst funktioner...
// Hvad med setname/getname???

// Maaske er Bundle et interface...
// Ogsaa compose()...
// Maaske vil vi kalde den noget andet...
// Meta meta

// Hmm, skal jo ogsaa styre extensions taenker jeg
// En extension skal vel heller ikke kunne installere en X, hvis brugeren
// ikke kan...
// Container can only contain actors surragates/stereotypes...

// Kan ikke se hvordan hvordan vi kan holder container configurations vaek fra subclasses.
// Som en protected metode -> all subclasses kan kalde den... Vi kan override den...
// Og saa smide en UOE?
// Hvis configurationen kommer ind gennem constructoren saa faar subklasses den jo...

abstract class AbstractBundle {

    AbstractBundle(ContextProvider<ContainerConfiguration> cp) {

    }

    AbstractBundle(Option... options) {

    }

    // Vil hellere have det i en bundle driver...
    // Vi gider ikke parse det hver gang...

    // En slags component driver er det vel????
    static class Option {

    }

    // @Inline

    static class F extends AbstractBundle {
        final ContextProvider<ContainerConfiguration> cp;

        F() {
            this(new ContextProvider<ContainerConfiguration>());
        }

        private F(ContextProvider<ContainerConfiguration> cp) {
            super(cp);
            this.cp = cp;
        }

        protected final F setName(String name) {
            cp.context().setName(name);
            return this;
        }
    }

    // Ideen er at power-power brugeren giver den til super klassen...

    // Vi vil gerne supportere linking af alle Bundles
    // Men lade en super brugere bestemme praecis hvad der kan vaere i en bundle/container...
    // F.eks. nej du kan ikke installere singletons...

    public static class ContextProvider<C> {

        C context;
        final AtomicInteger state = new AtomicInteger();

        public final C context() {
            throw new UnsupportedOperationException();
        }
    }
}
