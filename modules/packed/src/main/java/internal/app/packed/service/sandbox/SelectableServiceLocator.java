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
package internal.app.packed.service.sandbox;

import app.packed.base.Key;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.service.ServiceLocator;
import internal.app.packed.service.build.ServiceComposer;
import internal.app.packed.service.runtime.OldServiceLocator;
import internal.app.packed.service.runtime.ServiceSelection;

/**
 *
 */
public interface SelectableServiceLocator extends ServiceLocator {

    /**
     * Returns a service selection with all of the services in this locator.
     * 
     * @return a service selection with all of the services in this locator
     */
    ServiceSelection<?> selectAll();

    /**
     * Returns a service selection where the raw type of every service key is assignable to the specified type.
     * <p>
     * Unlike this method {@link #selectWithAnyQualifiers(Class)} this method will also select any
     * 
     * @param <T>
     *            the assignable type
     * @param type
     *            the assignable type
     * @return the service selection
     */
    <T> ServiceSelection<T> selectAssignableTo(Class<T> type);

    // Maaske drop withAnyQualifiers
    // T????? den giver ikke rigtig mening syntes jeg...
    //// Eller er det key delen vi selector paa?
    //// Altsaa selectOnKeyType
     <T> ServiceSelection<T> selectWithAnyQualifiers(Class<T> typePart);
//
//    /**
//     * @param <T>
//     *            the service type
//     * @param typePart
//     *            the type part of the key
//     * @return
//     */
//    <T> ServiceSelection<T> selectWithAnyQualifiers(TypeToken<T> typePart);
//

    /**
     * Spawns a new service locator by using a {@link ServiceComposer} to transmute this locator.
     * <p>
     * INSERT EXAMPLE
     * 
     * <p>
     * If you
     * 
     * @param action
     *            the transmutation action
     * @return the new service locator
     */
    OldServiceLocator spawn(ComposerAction<ServiceComposer> action);

    /**
     * Creates a new service locator via a service composer.
     * 
     * @param action
     *            the composition action
     * @return a new service locator
     * @see #driver()
     */
    static ServiceLocator of(ComposerAction<? super ServiceComposer> action) {
        throw new UnsupportedOperationException();
    }
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

    // Vi har faktisk 3.
    // Key Delen = Foo.class; (Ignores qualifiers)
    // Key delend.rawType = Foo.class
    // Key delen er assignable. <--- ved ikke hvor tit man skal bruge den

    // All whose raw type is equal to.. Don't know if it is
    default <T> ServiceSelection<T> selectRawType(Class<T> serviceType) {
        throw new UnsupportedOperationException();
    }
}
