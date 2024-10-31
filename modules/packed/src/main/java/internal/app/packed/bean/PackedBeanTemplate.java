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

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import app.packed.bean.BeanBuildLocal;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.context.ContextTemplate;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.PackedOperationTemplate;
import sandbox.application.LifetimeTemplate;

/** Implementation of {@link BeanTemplate}. */
public record PackedBeanTemplate(BeanKind beanKind, LifetimeTemplate lifetime, @Nullable Class<?> createAs, Map<PackedBeanBuildLocal<?>, Object> locals,
        @Nullable PackedOperationTemplate initializationTemplate) implements BeanTemplate {

    public PackedBeanTemplate(BeanKind kind) {
        this(kind, LifetimeTemplate.APPLICATION, null, Map.of(), (PackedOperationTemplate) OperationTemplate.defaults());
    }

    public PackedBeanTemplate withBeanClass(Class<?> beanClass) {
        return new PackedBeanTemplate(this.beanKind, this.lifetime, beanClass, this.locals, this.initializationTemplate);
    }

    public PackedBeanTemplate withLifetimeTemplate(LifetimeTemplate lifetime) {
        return new PackedBeanTemplate(this.beanKind, lifetime, this.createAs, this.locals, this.initializationTemplate);
    }

    /**
     * @param initialization
     * @return
     */
    public PackedBeanTemplate witInitialization(Function<OperationTemplate, OperationTemplate> configure) {
        PackedOperationTemplate ot = (PackedOperationTemplate) configure.apply(initializationTemplate);

        // We need to filter on some valid bean types I think
        // if (template.createAs.isPrimitive() || BeanSetup.ILLEGAL_BEAN_CLASSES.contains(template.createAs)) {
//      throw new IllegalArgumentException(template.createAs + " is not valid argument");
//  }

        return new PackedBeanTemplate(this.beanKind, this.lifetime, this.createAs, this.locals, ot);
    }

    /**
     * @param initialization
     * @return
     */
    public PackedBeanTemplate witInitialization(OperationTemplate ot) {
        return new PackedBeanTemplate(this.beanKind, this.lifetime, this.createAs, this.locals, (PackedOperationTemplate) ot);
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedBeanTemplate withBeanLocal(BeanBuildLocal<T> beanLocal, T value) {
        return new PackedBeanTemplate(this.beanKind, this.lifetime, this.createAs,
                PackedBuildLocal.initMap(this.locals, (PackedBeanBuildLocal<?>) beanLocal, value), this.initializationTemplate);
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
    public PackedBeanTemplate withInitialization(OperationTemplate initialization) {
        return witInitialization(initialization);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanTemplate withLifetime(LifetimeTemplate lifetime) {
        return withLifetimeTemplate(lifetime);
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
    public Optional<Class<?>> createAsX() {
        return Optional.ofNullable(createAs);
    }
}
