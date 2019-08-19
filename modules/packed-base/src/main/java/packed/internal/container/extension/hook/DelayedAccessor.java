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
package packed.internal.container.extension.hook;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;

/**
 *
 */
public abstract class DelayedAccessor {

    public final Class<?> sidecarType;
    public final BiConsumer<?, ?> consumer;

    DelayedAccessor(Class<?> sidecarType, BiConsumer<?, ?> consumer) {
        this.sidecarType = requireNonNull(sidecarType);
        this.consumer = requireNonNull(consumer);
    }

    public static final class SidecarFieldDelayerAccessor extends DelayedAccessor {
        public final PackedFieldHookApplicator<?> pra;

        public SidecarFieldDelayerAccessor(PackedFieldHookApplicator<?> pra, Class<?> sidecarType, BiConsumer<?, ?> consumer) {
            super(sidecarType, consumer);
            this.pra = requireNonNull(pra);
        }
    }

    public static final class SidecarMethodDelayerAccessor extends DelayedAccessor {
        public final PackedMethodHookApplicator<?> pra;

        public SidecarMethodDelayerAccessor(PackedMethodHookApplicator<?> pra, Class<?> sidecarType, BiConsumer<?, ?> consumer) {
            super(sidecarType, consumer);
            this.pra = requireNonNull(pra);
        }
    }
}
