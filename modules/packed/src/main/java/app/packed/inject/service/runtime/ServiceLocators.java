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

import app.packed.base.Reflectable;
import app.packed.base.TypeToken;
import app.packed.container.ComposerAction;
import app.packed.inject.service.OldServiceLocator;
import app.packed.inject.service.ServiceSelection;
import app.packed.inject.serviceexpose.ServiceComposer;
import packed.internal.inject.service.build.PackedServiceComposer;

/**
 *
 */
public interface ServiceLocators {

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
    default <T> ServiceSelection<T> selectWithAnyQualifiers(Class<T> typePart) {
        return selectWithAnyQualifiers(TypeToken.of(typePart));
    }

    /**
     * @param <T>
     *            the service type
     * @param typePart
     *            the type part of the key
     * @return
     */
    <T> ServiceSelection<T> selectWithAnyQualifiers(TypeToken<T> typePart);


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
    @Reflectable
    OldServiceLocator spawn(ComposerAction<ServiceComposer> action);


    /**
     * Creates a new service locator via a service composer.
     * 
     * @param action
     *            the composition action
     * @return a new service locator
     * @see #driver()
     */
    @Reflectable
    static OldServiceLocator of(ComposerAction<? super ServiceComposer> action) {
        return PackedServiceComposer.of(action);
    }
}
