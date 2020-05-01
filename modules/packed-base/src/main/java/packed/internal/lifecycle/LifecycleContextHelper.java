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
package packed.internal.lifecycle;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.lifecycle.LifecycleContext;

/**
 *
 */
public class LifecycleContextHelper {

    public static abstract class SimpleLifecycleContext implements LifecycleContext {

        private final String[] states;

        public SimpleLifecycleContext(String[] states) {
            this.states = requireNonNull(states);
        }

        /** {@inheritDoc} */
        @Override
        public String current() {
            return states[state()];
        }

        /** {@inheritDoc} */
        @Override
        public String desired() {
            return current();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStable() {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public Set<String> nextStates() {
            int state = state();
            return state == states.length - 1 ? Set.of() : Set.of(states[state + 1]);
        }

        protected abstract int state();
    }
}
