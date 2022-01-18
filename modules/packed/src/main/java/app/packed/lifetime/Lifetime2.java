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
package app.packed.lifetime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import app.packed.container.BaseAssembly;

/**
 *
 */
public interface Lifetime2 {
    Optional<Lifetime2> parent();

    boolean isCloseable();
}

// Things a lifetime does

// Determines a order between the components in the lifecycle when initializing/starting/stopping it it

// Individual initialization / stop

// Er lidt i tvivl om det her er 2 ting vi dealer med

// 

class SpringIntegration extends BaseAssembly {

    // m = Spring ApplicationContext
    Map<Class<?>, Object> m = new HashMap<>();

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    protected void build() {
        for (var e : m.entrySet()) {
            provideInstance(e.getValue()).provideAs((Class) e.getKey());
        }
    }
}