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
package app.packed.inject.service.runtime;

import app.packed.base.Key;
import app.packed.base.TypeToken;
import app.packed.inject.service.ServiceSelection;

/**
 *
 */
public interface SelectableServiceLocator {

}

interface Zandbox {

    // Ideen er lidt at vi tager alle keys. Hvor man kan fjerne 0..n qualififiers
    // og saa faa den specificeret key.

    // Kunne godt taenke mig at finde et godt navn.x
    // Naar en noegle er en super noegle???

    // may define any qualifiers
    default <T> ServiceSelection<T> select(Class<T> keyRawKeyType) {
        // select(Number.class) will select @Named("foo") Number but not Integer
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a service selection with all of the services in this locator with a {@link Key} whose {@link Key#rawType()}
     * is {@link Class#isAssignableFrom(Class) assignable} to the specified type.
     * <p>
     * Primitive types will automatically be boxed if specified.
     * 
     * @return a service selection with all of the services in this locator with a key whose raw type is assignable to the
     *         specified service type
     * @see Class#isAssignableFrom(Class)
     * @see Key#rawType()
     */
    // Hmm kan vi sige noget om actual type som vi producere???
    default <T> ServiceSelection<T> select(TypeToken<T> key) {
        // May define additional qualifiers
        throw new UnsupportedOperationException();
    }

    // Vi har faktisk 3.
    // Key Delen = Foo.class; (Ignores qualifiers)
    // Key delend.rawType = Foo.class
    // Key delen er assignable. <--- ved ikke hvor tit man skal bruge den

    // All whose raw type is equal to.. Don't know if it is
    default <T> ServiceSelection<T> selectRawType(Class<T> serviceType) {
        throw new UnsupportedOperationException();
    }
}
