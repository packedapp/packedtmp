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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanTemplate;
import app.packed.context.Context;
import app.packed.context.ContextTemplate;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.PackedOperationTemplate;
import sandbox.application.LifetimeTemplate;

/** Implementation of {@link BeanTemplate}. */
public record PackedBeanTemplate(BeanLifetime beanKind, LifetimeTemplate lifetime, @Nullable Class<?> createAs, Map<PackedBeanBuildLocal<?>, Object> locals,
        @Nullable PackedOperationTemplate initializationTemplate, Map<Class<?>, ContextTemplate> contexts) implements BeanTemplate {

    public static PackedBuilder builder(BeanLifetime kind) {
        return new PackedBuilder(kind);
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
    public PackedBeanInstaller newInstaller(ExtensionSetup installingExtension, AuthoritySetup<?> owner) {
        return new PackedBeanInstaller(this, installingExtension, owner);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<OperationTemplate> initializationX() {
        return Optional.ofNullable(initializationTemplate);
    }

    /** {@inheritDoc} */
    @Override
    public Map<Class<?>, ContextTemplate> contexts() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<?>> createAsSpecificClass() {
        return Optional.ofNullable(createAs);
    }

    public static final class PackedBuilder implements BeanTemplate.Builder {
        private final BeanLifetime beanKind;
        private LifetimeTemplate lifetime = LifetimeTemplate.APPLICATION;
        private Class<?> createAs = null;
        private final HashMap<Class<? extends Context<?>>, ContextTemplate> contexts = new HashMap<>();
        private Map<PackedBeanBuildLocal<?>, Object> locals = Map.of();
        private PackedOperationTemplate initializationTemplate = (PackedOperationTemplate) OperationTemplate.defaults();

        PackedBuilder(BeanLifetime beanKind) {
            this.beanKind = beanKind;
        }

        @Override
        public PackedBuilder beanClass(Class<?> beanClass) {
            this.createAs = beanClass;
            return this;
        }

        @Override
        public PackedBuilder lifetime(LifetimeTemplate lifetime) {
            this.lifetime = lifetime;
            return this;
        }

        @Override
        public PackedBuilder initialization(OperationTemplate initialization) {
            this.initializationTemplate = (PackedOperationTemplate) initialization;
            return this;
        }

        @Override
        public PackedBuilder initialization(Function<OperationTemplate, OperationTemplate> configure) {
            this.initializationTemplate = (PackedOperationTemplate) configure.apply(this.initializationTemplate);
            return this;
        }

        @Override
        public <T> PackedBuilder setLocal(BeanLocal<T> beanLocal, T value) {
            this.locals = PackedBuildLocal.initMap(this.locals, (PackedBeanBuildLocal<?>) beanLocal, value);
            return this;
        }

        @Override
        public PackedBeanTemplate build() {
            return new PackedBeanTemplate(beanKind, lifetime, createAs, locals, initializationTemplate, Map.copyOf(contexts));
        }

        /** {@inheritDoc} */
        @Override
        public Builder addContext(ContextTemplate context) {
            this.contexts.put(context.contextClass(), context);
            return this;
        }
    }
}
