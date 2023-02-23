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
package app.packed.extension.context;

import java.util.Map;
import java.util.Set;

import app.packed.container.Realm;
import app.packed.context.Context;

/**
 * All methods on this interface is only for informational purposes.
 */
// Context values er jo interessant for embedded operations...

// ContainerBuilder   ContainerHandle
// BeanBuilder        BeanHandle          BeanIntrospector, BeanVariable
// OperationBuilder   OperationHandle

// Simple
//// ContainerConfiguration, BeanConfiguration, OperationConfiguration

public interface ContextualizedElement {

    default Set<Class<? extends Context<?>>> contexts() {
        throw new UnsupportedOperationException();
    }

    /** {@return a set of the contexts available for this bean.} */
    // This method is mainly used for informational purposes.
    default Set<Class<? extends Context<?>>> contexts(Realm realm) {
        throw new UnsupportedOperationException();
    }

    // Returns the available Context
    /**
     * Returns the available {@link app.packed.extension.ContextValue context values}.
     *
     * @return
     */
    default Map<Class<? extends Context<?>>, Class<?>> contextValues() {
        throw new UnsupportedOperationException();
    }
}

// OperationContext = All embedded operations
// Og vil mene alle nested ogsaa.

// Det der er sjovt med nested operationer. Det er at man bestemmer ordenene.

// new