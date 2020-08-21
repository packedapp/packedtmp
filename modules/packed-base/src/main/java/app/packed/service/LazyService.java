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
package app.packed.service;

import app.packed.base.Named;

/**
 *
 */

// ServiceType: PROPTOTYPE, CONSTANT, LAZY
// I mean Singleton is actually okay because there is only a single one of the specified type available in a container
// But constant is also nice because it indicates whether or not, we can treat the service that are presented to us as
// a constant.. Lazy fx is a constant to.
// Important to note that the service is lazy, not the component. 

// Ideen er er at man gemmer den i et field

// Has a dependency on the component instance that defines it
abstract class LazyService<T> {

    // A component can define, 0, 1 or many services.
    // Creates a synthetic stateless component // .LazyService$$FooBar
    // LazyServiceConfiguration<T> ServiceExtension.provideLazy(Class<T> factory);
    // LazyServiceConfiguration<T> ServiceExtension.provideLazy(Factory<T> factory);

    // How does this work with shutdown???
    // Maaske bedre med @ProvideLazy... // Lazy always a constant.
    // Saa behoever vi heller ikke lave en synthetisk component

    abstract T compute();
}

class Usage {

    // Alternativ
    final LazyService<@Named("Hejhej") Long> ls = new LazyService<Long>() {

        @Override
        Long compute() {
            return 123L;
        }
    };
}