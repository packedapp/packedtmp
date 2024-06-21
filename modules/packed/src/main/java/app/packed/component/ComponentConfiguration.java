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

import app.packed.build.action.BuildActionable;

/**
 * The abstract configuration of a component.
 */

// Children??? Is it hierarchical??? May be, but you shouldn't get access to components outside your assembly
public abstract class ComponentConfiguration {

    /**
     * Checks that the container's assembly is still configurable.
     *
     * @throws IllegalStateException
     *             if the container's assembly is no longer configurable
     */
    protected final void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("The " + componentHandle().componentKind().name() + " is no longer configurable");
        }
    }

    /**
     * <p>
     * Typically a component configuration will define a such {@link app.packed.bean.BeanConfiguration#beanHandle}
     *
     * @return the component handle
     */
    protected abstract ComponentHandle componentHandle();

    /** {@return the path of the component} */
    public final ComponentPath componentPath() {
        return componentHandle().componentPath();
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
    @BuildActionable("component.addTags")
    public abstract ComponentConfiguration componentTag(String... tags);

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof ComponentConfiguration cc && componentHandle().equals(cc.componentHandle());
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return componentHandle().hashCode();
    }

    /**
     * {@return whether or not the component is still configurable}
     * <p>
     * Typically this is determined by whether or not the defining assembly of the component is still configurable.
     */
    public final boolean isConfigurable() {
        return componentHandle().isConfigurable();
    }
}
