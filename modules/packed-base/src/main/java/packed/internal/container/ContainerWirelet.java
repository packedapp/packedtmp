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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;

/**
 *
 */
// Maybe its just component wirelets....
// And then we only have Extension wirelets????

//ArchWirelets taenker det maaske mere er en annoteringen man kan bruge paa bundlen...
// F.eks. alle containeres navn skal starte med "Xyz" hvis der er over 10 componenter...
// Er det noget dev tools???? Det taenker jeg...
// Eller er det noget vi gider validere i production???
// Altsaa det bliver vel smidt vaek naar man har lavet et image....

// Taenker det er noget man kan teste for ArchCheck.check(new DooBundle());

// ComponentWirelet???
public abstract class ContainerWirelet implements Wirelet {

    abstract void process(WireletPack c);

    /** A wirelet that will set the name of the container. Used by {@link Wirelet#name(String)}. */
    public static final class ContainerNameWirelet extends ContainerWirelet {

        /** The (checked) name to override with. */
        public final String name;

        /**
         * Creates a new option
         * 
         * @param name
         *            the name to override any existing container name with
         */
        public ContainerNameWirelet(String name) {
            this.name = checkName(name);
        }

        /** {@inheritDoc} */
        @Override
        void process(WireletPack c) {
            c.newName = this;// will override any set previously
        }

        /**
         * Checks the name of the component.
         * 
         * @param name
         *            the name to check
         * @return the name if valid
         */
        public static String checkName(String name) {
            requireNonNull(name, "name is null");
            if (name != null) {

            }
            return name;
        }
    }
}
