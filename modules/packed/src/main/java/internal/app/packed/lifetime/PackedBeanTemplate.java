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

import java.util.List;
import java.util.Optional;

import app.packed.bean.BeanKind;
import app.packed.util.Nullable;
import internal.app.packed.bean.PackedBeanHandleBuilder;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.operation.OperationTemplate;

/**
 *
 */
public record PackedBeanTemplate(BeanKind kind, OperationTemplate bot, @Nullable Class<?> createAs) implements BeanTemplate {

    public PackedBeanTemplate(BeanKind kind) {
        this(kind, null, Object.class);
    }

    /** {@inheritDoc} */
    public PackedBeanTemplate withOperationTemplate(OperationTemplate bot) {
        return new PackedBeanTemplate(kind, bot, createAs);
    }

    /** {@inheritDoc} */
    @Override
    public BeanTemplate createAs(Class<?> createAs) {
        if (createAs.isPrimitive() || PackedBeanHandleBuilder.ILLEGAL_BEAN_CLASSES.contains(createAs)) {
            throw new IllegalArgumentException(createAs + " is not valid argument");
        }
        return new PackedBeanTemplate(kind, bot, createAs);
    }

    @Override
    public BeanTemplate createAsBeanClass() {
        return new PackedBeanTemplate(kind, bot, null);
    }

    /** {@inheritDoc} */
    @Override
    public BeanTemplate inLifetimeOperationContext(int index, ContextTemplate template) {
        throw new UnsupportedOperationException();
    }

    public record PackedBeanTemplateDescriptor(PackedBeanTemplate pbt) implements BeanTemplate.Descriptor {

        /** {@inheritDoc} */
        @Override
        public Optional<Class<?>> createAs() {
            return Optional.ofNullable(pbt.createAs);
        }

        /** {@inheritDoc} */
        @Override
        public List<OperationTemplate.Descriptor> lifetimeOperations() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BeanKind beanKind() {
            return pbt.kind;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor descriptor() {
        return new PackedBeanTemplateDescriptor(this);
    }
}
