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
package app.packed.inject.sandbox;

import java.lang.reflect.Member;

/**
 *
 */
public enum InjectionScope {

    BUNDLE,

    CLASS,

    MEMBER_OR_FUNCTION;

}
// Hvad har Wirelet af scope??? Protortpe eller Singleton taenker jeg

enum OldInjectionScope2 {

    CLASS,

    /**
     * A scope to a particular {@link Member (field, constructor, or method)}.
     * 
     */
    MEMBER,

    PROTOTYPE,

    REQUEST, // CONTAINER, LOCATOR

    SINGLETON;
}