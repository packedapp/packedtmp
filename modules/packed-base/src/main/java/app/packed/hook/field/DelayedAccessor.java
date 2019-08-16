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
package app.packed.hook.field;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;

/**
 *
 */
public abstract class DelayedAccessor {

    public static final class SidecarDelayerAccessor extends DelayedAccessor {
        public final Class<?> sidecarType;
        public final BiConsumer<?, ?> consumer;
        public final PackedRuntimeAccessor<?> pra;

        public SidecarDelayerAccessor(PackedRuntimeAccessor<?> pra, Class<?> sidecarType, BiConsumer<?, ?> consumer) {
            this.pra = requireNonNull(pra);
            this.sidecarType = requireNonNull(sidecarType);
            this.consumer = requireNonNull(consumer);
        }
    }
}
