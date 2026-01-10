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
package app.packed.lifecycle.runtime;

import java.util.Optional;
import java.util.concurrent.CancellationException;

import app.packed.lifecycle.RunState;

/**
 * Useful for example together {@link app.packed.lifetime.OnStop}
 */
// I don't think ever initializing->terminated
public interface RunStateTransition {

    /** {@return any failure that caused the transition} */
    Optional<Throwable> failure();

    /** {@return the run state we are transition from} */
    RunState from();

    default boolean isCancelled() {
        return failure().map(t -> CancellationException.class.isAssignableFrom(t.getClass())).orElse(false);
    }

    /** {@return the run state we are transition to} */
    RunState to();
}
