/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.service.bridge.old;

import java.lang.annotation.Annotation;
import java.util.Set;

import app.packed.binding.Key;

/**
 *
 */

// optional requirement into mandatory // addRequirement (will turn optional into requirement, or create one)
// optional requirement with default value instead of empty if missing

// decorate // What if we take something from the incoming container

// rekey

// map / replace
//// exports

// remove | retain
//// exports
//// optional requirements (// optional requirements into missing)

// peek
//// exports
//// requirement
//// optional requirement????

// provide
//// any requirement (removed afters)
//// exports -> we simply add it to exports (added afterwards)
// Taenker de bliver applied i omvendt raekkefoelge hvis man har flere
public interface ServiceToChildTransformer {

    // contract.foreachOptional.remove
    ServiceToChildTransformer ignoreAllOptionals();

    ServiceToChildTransformer ignoreOptionals(Key<?>... keys);

    <T> ServiceToChildTransformer provideInstance(Class<T> key, T instance);

    /**
     * @param <T>
     * @param key
     * @param instance
     * @return
     */
    <T> ServiceToChildTransformer provideInstance(Key<T> key, T instance);

    /**
     * @param a
     * @return
     */
    ServiceToChildTransformer rekeyAllWithQualifier(Annotation a);

    Set<Key<?>> requires();

    // Mutable? I think so and then skip ignore optionals
    Set<Key<?>> requiresOptional();
}
