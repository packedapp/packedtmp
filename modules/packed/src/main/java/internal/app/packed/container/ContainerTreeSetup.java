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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import app.packed.container.ContainerTreeMirror;

/**
 *
 */

//Contexts (ContainerTree+Beans+Operations)
//Domains (ContainerTree)


// Domains?

//// De her skal ikke bruge ContainerTreeSetup
// Contexts fixed by context owner, maybe allow propagation
// Lifetime -> growable but stored in ContainerSetup
// ContainerLocal fixed when defining it, Application, Container_Lifetime, Container
// Wirelets fixed by wirelet definer. Cannot be changed
public sealed abstract class ContainerTreeSetup {

    public final ContainerSetup root;

    ContainerTreeSetup(ContainerSetup root) {
        this.root = requireNonNull(root);
    }

    public abstract ContainerTreeMirror mirror();

    public final class SingleContainerTreeSetup extends ContainerTreeSetup {

        public SingleContainerTreeSetup(ContainerSetup root) {
            super(root);
        }

        /** {@inheritDoc} */
        @Override
        public ContainerTreeMirror mirror() {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return 1;
        }
    }
}
