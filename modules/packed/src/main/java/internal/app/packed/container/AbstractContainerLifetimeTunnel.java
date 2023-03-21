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
package internal.app.packed.container;

import java.util.Set;

import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.util.Key;
import sandbox.extension.container.ContainerLifetimeTunnel;

/**
 *
 */
public non-sealed interface AbstractContainerLifetimeTunnel extends ContainerLifetimeTunnel {

    void build(LeafContainerOrApplicationBuilder builder);

    public record ConstantContainerLifetimeTunnel(Key<?> key, Object constant) implements AbstractContainerLifetimeTunnel {

        public ConstantContainerLifetimeTunnel {
            // TODO check assignable to constant
        }

        /** {@inheritDoc} */
        @Override
        public Class<? extends Extension<?>> extensionClass() {
            return BaseExtension.class;
        }

        /** {@inheritDoc} */
        @Override
        public Set<Key<?>> keys() {
            return Set.of(key);
        }

        /** {@inheritDoc} */
        @Override
        public ContainerLifetimeTunnel rekey(Key<?> from, Key<?> to) {
            if (!key.equals(from)) {
                throw new IllegalArgumentException("from key must be " + key);
            }
            return new ConstantContainerLifetimeTunnel(to, constant);
        }

        /** {@inheritDoc} */
        @Override
        public void build(LeafContainerOrApplicationBuilder builder) {

        }
    }
}
