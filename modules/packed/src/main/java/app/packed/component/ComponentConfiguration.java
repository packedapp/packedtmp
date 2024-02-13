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

/**
 * The abstract configuration of a component.
 */
// We do not have a common ComponentHandle. Because extensions might define their own component configuration.

// Children??? Is it hierarchical???
public interface ComponentConfiguration {

    /** {@return the path of the component} */
    ComponentPath componentPath();

    /**
     * Adds the specified tags to the set of component tags.
     *
     * @param tags
     *            tags to add to the component
     * @return this configuration
     *
     * @see ComponentMirror#componentTags()
     */
    ComponentConfiguration componentTag(String... tags);

    /**
     * {@return whether or not the component is still configurable}
     * <p>
     * Typically this is determined by whether or not the defining assembly of the component is still configurable.
     */
    boolean isConfigurable();


    // A unique component id for the component in the application, once installed it will not change
    // Path can be updated because of naming
    // Long???
    default long componentId() {
        return 0;
    }
}
