/*
 * Copyright (c) 2026 Kasper Nielsen.
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
     * Check that the component is still configurable by its owner.
     *
     * @throws IllegalStateException
     *             if the component is no longer configurable
     */
    protected final void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("The " + componentPath().componentKind().name() + " is no longer configurable");
        }
    }

    /** {@return the path of this component} */
    // Why componentPath and not path if tags()
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
    public abstract ComponentConfiguration tag(String... tags);

    /** {@return a set of all tags on this component} */
    public abstract Set<String> tags();

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof ComponentConfiguration cc && handle().equals(cc.handle());
    }

    /** {@return the underlying handle of this component} */
    protected abstract ComponentHandle handle();

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return handle().hashCode();
    }

    /** {@return whether or not the component is still configurable} */
    public final boolean isConfigurable() {
        return handle().isConfigurable();
    }
}
