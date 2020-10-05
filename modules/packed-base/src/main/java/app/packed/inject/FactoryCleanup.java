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
package app.packed.inject;

import java.util.function.Supplier;

import app.packed.component.App;
import app.packed.container.BaseBundle;

/**
 *
 */

// Need to check return type

// The supplier ddd must create instances of Foo, but was

// Naming
public class FactoryCleanup extends BaseBundle {

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void configure() {
        Supplier s = () -> 23;

        providePrototype(new Factory0<String>(s) {}).export();
    }

    public static void main(String[] args) {
        System.out.println(App.of(new FactoryCleanup()).use(String.class));

    }
}
