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
package packed.internal.lifecycle.old;

import static java.util.Objects.requireNonNull;

import app.packed.state.sandbox.InstanceState;

/**
 *
 */
public abstract class PackedLifecycleState {

    public static final PackedLifecycleState CONSTRUCT_INITALIZING = new ConstructLifecycleState(InstanceState.UNINITIALIZED);

    public static final PackedLifecycleState CONSTRUCT_INITIALIZED = new ConstructLifecycleState(InstanceState.INITIALIZED);

    public abstract InstanceState state();

    private static class ConstructLifecycleState extends PackedLifecycleState {

        private final InstanceState state;

        private ConstructLifecycleState(InstanceState state) {
            this.state = requireNonNull(state);
        }

        /** {@inheritDoc} */
        @Override
        public InstanceState state() {
            return state;
        }
    }
}
