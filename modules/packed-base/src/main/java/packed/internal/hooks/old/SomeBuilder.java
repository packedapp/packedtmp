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
package packed.internal.hooks.old;

import static java.util.Objects.requireNonNull;


// Jeg tror de her er fordi, 

public final class SomeBuilder {
    //final InstantiatorBuilder ib;

    final Class<?>[] implementation;

    final extensionClass type;

    SomeBuilder(Class<?>[] implementation, extensionClass type, int modifiers) {
        this.implementation = requireNonNull(implementation);
        this.type = requireNonNull(type);
        //this.ib = Infuser.of(MethodHandles.lookup()).findConstructorFor(implementation);
    }

    SomeBootstrapModel bootstrap() {
        throw new UnsupportedOperationException();
    }
    enum extensionClass {
        CLASS, CONSTRUCTOR, FIELD, METHOD;
    }

    static enum Scope {
        BOOTSTRAP, BUILD, BUILD_INSTANCE; // skal vi separere mellem BUILD_INSTANCE og build_Construct
    }
}