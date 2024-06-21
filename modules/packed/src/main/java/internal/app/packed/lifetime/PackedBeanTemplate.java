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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.util.Nullable;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.application.LifetimeTemplate;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.operation.OperationTemplate;

/**
 *
 */
public record PackedBeanTemplate(BeanKind kind, LifetimeTemplate lifetime, OperationTemplate bot, @Nullable Class<?> createAs) implements BeanTemplate {

    public PackedBeanTemplate(BeanKind kind) {
        this(kind, LifetimeTemplate.APPLICATION, null, Object.class);
    }

    // Er det lifetime operationer???
    /** {@inheritDoc} */
    public PackedBeanTemplate withOperationTemplate(OperationTemplate bot) {
        return new PackedBeanTemplate(kind, lifetime, bot, createAs);
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor descriptor() {
        return new PackedBeanTemplateDescriptor(this);
    }

    public static PackedBeanTemplate configure(PackedBeanTemplate template, Consumer<? super Configurator> configure) {
        PackedBeanTemplateConfigurator c = new PackedBeanTemplateConfigurator(template);
        configure.accept(c);
        return c.template;
    }

    public static final class PackedBeanTemplateConfigurator implements BeanTemplate.Configurator {

        private PackedBeanTemplate template;

        private PackedBeanTemplateConfigurator(PackedBeanTemplate template) {
            this.template = template;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator createAs(Class<?> clazz) {
            if (template.createAs.isPrimitive() || PackedBeanInstaller.ILLEGAL_BEAN_CLASSES.contains(template.createAs)) {
                throw new IllegalArgumentException(template.createAs + " is not valid argument");
            }
            this.template = new PackedBeanTemplate(template.kind, template.lifetime, template.bot, template.createAs);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator createAsBeanClass() {
            this.template = new PackedBeanTemplate(template.kind, template.lifetime, template.bot, null);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator inContextForLifetimeOperation(int index, ContextTemplate template) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator lifetime(LifetimeTemplate lifetime) {
            this.template = new PackedBeanTemplate(template.kind, lifetime, template.bot, template.createAs);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public <T> Configurator localSet(BeanLocal<T> beanLocal, T value) {
            throw new UnsupportedOperationException();
        }
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

        /** {@inheritDoc} */
        @Override
        public Map<Class<?>, ContextTemplate.Descriptor> contexts() {
            throw new UnsupportedOperationException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public BeanTemplate reconfigure(Consumer<? super Configurator> configure) {
        return PackedBeanTemplate.configure(this, configure);
    }
}
