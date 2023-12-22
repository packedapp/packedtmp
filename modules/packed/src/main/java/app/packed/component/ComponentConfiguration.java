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

import static java.util.Objects.requireNonNull;

/**
 * The abstract configuration of a component.
 */
public abstract class ComponentConfiguration implements Component {

    private final ComponentHandle handle;

    protected ComponentConfiguration() {
        this.handle = null;
    }

    protected ComponentConfiguration(ComponentHandle handle) {
        this.handle = requireNonNull(handle, "handle is null");
    }

    /** {@return the path of the component} */
    @Override
    public final ComponentPath componentPath() {
        return handle.componentPath();
    }

    public ComponentConfiguration componentTag(String... tags) {
        handle.componentTags(tags);
        return this;
    }
}
