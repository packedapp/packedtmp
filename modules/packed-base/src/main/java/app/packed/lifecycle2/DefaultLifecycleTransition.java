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
package app.packed.lifecycle2;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.lifecycle.LifecycleTransition;

public final class DefaultLifecycleTransition implements LifecycleTransition {

    @Nullable
    private final String action;

    private final String from;
    private final String to;

    public DefaultLifecycleTransition(String from, String to, @Nullable String action) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
        this.action = action;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> action() {
        return Optional.ofNullable(action);
    }

    /** {@inheritDoc} */
    @Override
    public String from() {
        return from;
    }

    /** {@inheritDoc} */
    @Override
    public String to() {
        return to;
    }

    @Override
    public String toString() {
        return "Transition ['" + from + "' -> '" + to + "' , action = " + action + "]";
    }
}