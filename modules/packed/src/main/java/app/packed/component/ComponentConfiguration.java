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
package app.packed.component;

import java.util.Set;

import app.packed.application.ApplicationConfiguration;
import app.packed.bean.BeanConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.namespace.NamespaceConfiguration;
import app.packed.operation.OperationConfiguration;

/** Configuration of a component. */
public abstract sealed class ComponentConfiguration
        permits ApplicationConfiguration, BeanConfiguration, ContainerConfiguration, NamespaceConfiguration, OperationConfiguration {

    /**
     * Check that the component is still configurable.
     *
     * @throws IllegalStateException
     *             if the component is no longer configurable
     */
    protected final void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("The " + componentPath().componentKind().name() + " is no longer configurable");
        }
    }

    /** {@return the path of the component} */
    public final ComponentPath componentPath() {
        return handle().componentPath();
    }

    /**
     * Adds the specified tags to the set of component tags.
     *
     * @param tags
     *            tags to add to the component
     * @return this configuration
     *
     * @see ComponentMirror#componentTags()
     */
    public abstract ComponentConfiguration componentTag(String... tags);

    public abstract Set<String> componentTags();

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof ComponentConfiguration cc && handle().equals(cc.handle());
    }

    /**
     * <p>
     * Typically a component configuration will define a such {@link app.packed.bean.BeanConfiguration#beanHandle}
     *
     * @return the component handle
     */
    protected abstract ComponentHandle handle();

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return handle().hashCode();
    }

    /**
     * {@return whether or not the component is still configurable}
     * <p>
     * Typically this is determined by whether or not the defining assembly of the component is still configurable.
     */
    // I think the handle is configurable after the assembly has closed????
    // So I think handle.isConfigurable!=configuration.configurable
    public final boolean isConfigurable() {
        return handle().isConfigurationConfigurable();
    }
}
