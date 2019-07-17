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
package app.packed.contract;

import java.util.Map;
import java.util.Set;

import app.packed.config.ConfigSite;
import app.packed.util.Key;

//Contracts for Containers/Bundles, Hosts
/**
 *
 */
// BundleDescriptor extends Contract

// Host can never have required services...
// Or hooks....
// Name
// Description
// Version

// Merge to contracts.... All fragments, needs a merge method

// Contracts are all about the API.
// Descriptors are all about the implementation details
public final class Contract {

    // Of cource, descriptor should have one as well...
    ConfigSite configSite; // How did we create this contract....

    // String Description.. But it is neither bundle or container. It is the description of the contract...

    // Metoder til at teste om en contract er opfyldt... Evt et diff....

    public Services services() {
        throw new UnsupportedOperationException();
    }

    public static Contract compose(Contract... contracts) {
        // Maaske finder der en mere elegant maade. Taenker specificationer er hirakiske...
        // Altsaa F.eks. J2EE = JaxRS + JPA
        throw new UnsupportedOperationException();
    }

    // extensionOf --- find et bedre navn --- men ihvertfald 3.2 er en videreudvikling af 3.1

    /**
     * 
     */
    static abstract class ContractFragment {
        // Hvis vi nogensinde faar brug for en basis klasse..
        // ContractVisitor...
    }

    static interface ContractVisitor {
        void visitContract(Contract contract);

        default void visitFragment(ContractFragment fragment) {
            // if (fragment instanceof Services) {
            // visitServices(Services)
            // }
        }
    }

    public final class Services {

        // Optional<String> exportDescription(Key<?> key);
        //
        // Set<String> exportModuleRestrictions(Key<?> key); // or maybe just Map<Key<?>, Set<String>();

        public Set<Key<?>> exports() {
            throw new UnsupportedOperationException();
        }

        public Map<Key<?>, String> exportsDescriptions() {
            throw new UnsupportedOperationException();
        }

        // ApiStatus exportApiStatus(Key<?> key);
        public Map<Key<?>, Set<String>> exportsModuleRestrictions() {
            throw new UnsupportedOperationException();
        }

        public Set<Key<?>> optionals() {
            throw new UnsupportedOperationException();
        }

        public Set<Key<?>> requires() {
            throw new UnsupportedOperationException();
        }
    }
}
// You can create a Application Server-> That can have a Webserver