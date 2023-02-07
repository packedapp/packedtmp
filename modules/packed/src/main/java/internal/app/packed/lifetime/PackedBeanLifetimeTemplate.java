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
package internal.app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import app.packed.bean.BeanKind;
import app.packed.framework.Nullable;
import app.packed.lifetime.BeanLifetimeTemplate;
import app.packed.operation.BeanOperationTemplate;

/**
 *
 */
public final class PackedBeanLifetimeTemplate implements BeanLifetimeTemplate {

    @Nullable
    public final BeanOperationTemplate bot;

    public final BeanKind kind;

    public PackedBeanLifetimeTemplate(BeanKind kind) {
        this.kind = requireNonNull(kind);
        this.bot = null;
    }

    PackedBeanLifetimeTemplate(BeanKind kind, BeanOperationTemplate bot) {
        this.kind = requireNonNull(kind);
        this.bot = bot;
    }

    public static final class PackedBuilder implements BeanLifetimeTemplate.Builder {

        @Nullable
        BeanOperationTemplate bot = BeanOperationTemplate.defaults();

        /** {@inheritDoc} */
        @Override
        public Builder autoStart() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BeanLifetimeTemplate build() {
            return new PackedBeanLifetimeTemplate(BeanKind.MANYTON, bot);
        }

        /** {@inheritDoc} */
        @Override
        public Builder createdAs(Class<?> clazz) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Builder withLifetime(BeanOperationTemplate bot) {
            this.bot = bot;
            return this;
        }

    }
}
