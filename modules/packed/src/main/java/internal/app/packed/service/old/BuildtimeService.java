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
package internal.app.packed.service.old;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.service.Key;
import internal.app.packed.lifetime.LifetimeObjectArena;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * Instances of this class are only exposed as a to end users if {@link #isKeyFrozen}.
 */
abstract class BuildtimeService {

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with . In which the case the declaring class needs to be constructor injected before the
     * providing method can be invoked.
     */
    public final Key<?> key;

    BuildtimeService(Key<?> key) {
        this.key = requireNonNull(key);
    }

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract MethodHandle newRuntimeNode(LifetimeObjectArena context);
}
