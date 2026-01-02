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
import app.packed.context.Context;
import app.packed.util.Nullable;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.context.ContextModel;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.PackedOperationTemplate;
import sandbox.application.LifetimeTemplate;

/** Implementation of {@link BeanTemplate}. */
public record PackedBeanTemplate(BeanLifetime beanKind, LifetimeTemplate lifetime, @Nullable Class<?> createAs, Map<PackedBeanBuildLocal<?>, Object> locals,
        @Nullable PackedOperationTemplate initializationTemplate, Map<Class<?>, ContextModel> contextxs) {

    public static PackedBuilder builder(BeanLifetime kind) {
        return new PackedBuilder(kind);
    }

    /** Returns a new builder initialized with this template's values. */
    public PackedBuilder builder() {
        return new PackedBuilder(this);
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
    public Optional<PackedOperationTemplate> initializationX() {
        return Optional.ofNullable(initializationTemplate);
    }

    /**
     * Normally a bean is constructed as the ben
     * <p>
     * Empty means create as bean class
     *
     * @return
     *
     * @see BeanTemplate#createAs(Class)
     * @see BeanTemplate#createAsBeanClass()
     */
    public Optional<Class<?>> createAsSpecificClass() {
        return Optional.ofNullable(createAs);
    }

    public static final class PackedBuilder {
        private final BeanLifetime beanKind;
        private LifetimeTemplate lifetime = LifetimeTemplate.APPLICATION;
        private Class<?> createAs = null;
        private final HashMap<Class<? extends Context<?>>, ContextModel> contexts = new HashMap<>();
        private Map<PackedBeanBuildLocal<?>, Object> locals = Map.of();
        private PackedOperationTemplate initializationTemplate = PackedOperationTemplate.DEFAULTS;

        PackedBuilder(BeanLifetime beanKind) {
            this.beanKind = beanKind;
        }

        @SuppressWarnings("unchecked")
        PackedBuilder(PackedBeanTemplate template) {
            this.beanKind = template.beanKind();
            this.lifetime = template.lifetime();
            this.createAs = template.createAs();
            this.locals = template.locals().isEmpty() ? Map.of() : new HashMap<>(template.locals());
            this.initializationTemplate = template.initializationTemplate();
            template.contextxs().forEach((k, v) -> this.contexts.put((Class<? extends Context<?>>) k, v));
        }

        public PackedBuilder initialization(PackedOperationTemplate initialization) {
            this.initializationTemplate = initialization;
            return this;
        }

        public PackedBuilder initialization(Function<PackedOperationTemplate, PackedOperationTemplate> configure) {
            this.initializationTemplate = configure.apply(this.initializationTemplate);
            return this;
        }

        public <T> PackedBuilder setLocal(BeanLocal<T> beanLocal, T value) {
            this.locals = PackedBuildLocal.initMap(this.locals, (PackedBeanBuildLocal<?>) beanLocal, value);
            return this;
        }

        public PackedBeanTemplate build() {
            return new PackedBeanTemplate(beanKind, lifetime, createAs, locals, initializationTemplate, Map.copyOf(contexts));
        }

        /** {@inheritDoc} */
        public PackedBuilder addContext(Class<? extends Context<?>> contextClass) {
            this.contexts.put(contextClass, ContextModel.of(contextClass));
            return this;

        }
    }
}
