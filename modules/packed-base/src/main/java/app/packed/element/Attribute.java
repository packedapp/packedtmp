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
package app.packed.element;

import java.lang.invoke.MethodHandles;

import app.packed.base.TypeLiteral;

/**
 *
 */

// Maaske har vi bare en realm.... Og ikke en Extension...

public interface Attribute<T> {

    /**
     * Returns the name of the attribute.
     * 
     * @return the name of the attribute
     */
    String name();// simpleName???

    // shortname
    // DependsOn

    // name
    // ServiceExtension#DependsOn

    // fullname
    // app.packed.service.ServiceExtension#DependsOn

    // name = DependsOn, fullName=ServiceExtension#DependsOn

    // Bliver der noget saa maa folk skrive det fuldt ud selv.
    // Vi kan heller ikke checke ting der er loaded med forskelling class loaders.

    Class<?> rawType();

    Class<?> realm();// From the lookup object

    static <T> Attribute<T> of(MethodHandles.Lookup lookup, String name, Class<T> type, Option... options) {
        throw new UnsupportedOperationException();
    }

    static <T> Attribute<T> of(MethodHandles.Lookup lookup, String name, TypeLiteral<T> type, Option... options) {
        throw new UnsupportedOperationException();
    }

    interface Option {
        static Option readable() {
            throw new UnsupportedOperationException();
        }
    }
}

// Har nogle andre options?????
interface ComponentAttribute<T> extends Attribute<T> {

}