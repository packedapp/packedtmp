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
package internal.app.packed.bean;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanTemplate;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.context.publish.ContextTemplate;
import internal.app.packed.extension.ExtensionSetup;
import sandbox.application.LifetimeTemplate;

/** Implementation of {@link BeanTemplate}. */
public record PackedBeanTemplate(BeanKind kind, LifetimeTemplate lifetime, OperationTemplate bot, @Nullable Class<?> createAs,
        Map<PackedBeanLocal<?>, Object> beanLocals) implements BeanTemplate {

    public PackedBeanTemplate(BeanKind kind) {
        this(kind, LifetimeTemplate.APPLICATION, null, Object.class, Map.of());
    }

    // Er det lifetime operationer???
    /** {@inheritDoc} */
    public PackedBeanTemplate withOperationTemplate(OperationTemplate bot) {
        return new PackedBeanTemplate(kind, lifetime, bot, createAs, beanLocals);
    }

    /**
     * Create a new bean installer from this template.
     *
     * @param installingExtension
     *            the extension that is installing the bean
     * @param owner
     *            the owner of the bean
     * @return the new bean installer
     */
    public PackedBeanInstaller newInstaller(ExtensionSetup installingExtension, AuthoritySetup owner) {
        return new PackedBeanInstaller(this, installingExtension, owner);
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor descriptor() {
        return new PackedBeanTemplateDescriptor(this);
    }

    /** {@inheritDoc} */
    @Override
    public BeanTemplate reconfigure(Consumer<? super Configurator> configure) {
        return PackedBeanTemplate.reconfigureExisting(this, configure);
    }

    /**
     * Reconfigures an existing bean template.
     *
     * @param existing
     *            the bean template to reconfigure
     * @param action
     *            the reconfiguration action
     * @return the new bean template
     */
    public static PackedBeanTemplate reconfigureExisting(PackedBeanTemplate existing, Consumer<? super Configurator> action) {
        PackedBeanTemplateConfigurator c = new PackedBeanTemplateConfigurator(existing);
        action.accept(c);
        return c.template;
    }

    /** Implementation of {@link BeanTemplate.Configurator} */
    public static final class PackedBeanTemplateConfigurator implements BeanTemplate.Configurator {

        /** The template we are configuring. */
        private PackedBeanTemplate template;

        private PackedBeanTemplateConfigurator(PackedBeanTemplate template) {
            this.template = template;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator createAs(Class<?> clazz) {
            if (template.createAs.isPrimitive() || BeanSetup.ILLEGAL_BEAN_CLASSES.contains(template.createAs)) {
                throw new IllegalArgumentException(template.createAs + " is not valid argument");
            }
            return init(template.kind, template.lifetime, template.bot, template.createAs, template.beanLocals);
        }

        /** {@inheritDoc} */
        @Override
        public Configurator createAsBeanClass() {
            return init(template.kind, template.lifetime, template.bot, null, template.beanLocals);
        }

        /** {@inheritDoc} */
        @Override
        public Configurator inContextForLifetimeOperation(int index, ContextTemplate template) {
            return null;
        }

        private Configurator init(BeanKind kind, LifetimeTemplate lifetime, OperationTemplate bot, @Nullable Class<?> createAs,
                @Nullable Map<PackedBeanLocal<?>, Object> beanLocals) {
            this.template = new PackedBeanTemplate(kind, lifetime, bot, createAs, beanLocals);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator lifetime(LifetimeTemplate lifetime) {
            return init(template.kind, lifetime, template.bot, template.createAs, template.beanLocals);
        }

        /** {@inheritDoc} */
        @Override
        public <T> Configurator localSet(BeanLocal<T> beanLocal, T value) {
            return init(template.kind, template.lifetime, template.bot, template.createAs,
                    PackedBuildLocal.initMap(template.beanLocals, (PackedBeanLocal<?>) beanLocal, value));
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
}
