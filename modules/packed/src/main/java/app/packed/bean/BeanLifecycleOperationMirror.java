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
package app.packed.bean;

import java.util.Optional;

import app.packed.lifetime.LifetimeOperationMirror;
import app.packed.operation.OperationMirror;

/**
 * An operation that is invoked doing lifecycle events on the bean.
 */
// Mit eneste problem er om vi fx har operationer der baade kan kalde paa flere tidspunkter??

public class BeanLifecycleOperationMirror extends OperationMirror {

    /** {@return the lifetime operation this operation is a part of.} */
    
    // IDK, supportere vi Lifecycle events there ikke har en Lifetime operation???
    // Saa er det ikke en lifetime. Fx restart
    public LifetimeOperationMirror lifetime() {
        throw new UnsupportedOperationException();
    }

    // Previous on bean?
    // Previous on Lifetime
    // Maybe better to leave out
    Optional<BeanLifecycleOperationMirror> previous() {
        return Optional.empty();
    }
}
