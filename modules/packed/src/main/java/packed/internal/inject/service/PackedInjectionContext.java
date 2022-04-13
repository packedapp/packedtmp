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
package packed.internal.inject.service;

import java.util.Map;

import app.packed.base.Key;
import app.packed.inject.InjectionContext;
import app.packed.inject.service.Service;

/** Implementation of {@link InjectionContext}. */
public record PackedInjectionContext(Class<?> targetClass, Map<Key<?>, Service> asMap) implements InjectionContext {

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "InjectionContext[" + targetClass.getCanonicalName() + "]: " + asMap.keySet();
    }
}