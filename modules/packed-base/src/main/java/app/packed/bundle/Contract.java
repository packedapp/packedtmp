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
package app.packed.bundle;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import app.packed.util.Key;

/**
 *
 */
// BundleDescriptor extends Contract

// Host can never have required services...
// Or hooks....
// Name
// Description
// Version
public final class Contract {

    // Metoder til at teste om en contract er opfyldt... Evt et diff....

    public Services services() {
        throw new UnsupportedOperationException();
    }

    public static Contract compose(Contract... contracts) {
        // Maaske finder der en mere elegant maade. Taenker specificationer er hirakiske...
        // Altsaa F.eks. J2EE = JaxRS + JPA
        throw new UnsupportedOperationException();
    }

    static class ContractFragment {
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

    public static final class Fasteners {

        // Permissions-> For AOP, For Invocation, for da shizzla

        public Set<Class<? extends Annotation>> annotatedFieldsHooks() {
            return Set.of();
        }

        public Set<Class<? extends Annotation>> annotatedMethods() {
            return Set.of();
        }

        public Set<Class<? extends Annotation>> annotatedTypes() {
            return Set.of();
        }
    }

    // change to class
    public final class Services {

        Services(ContractBuilder.Services servicesxz) {

        }

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