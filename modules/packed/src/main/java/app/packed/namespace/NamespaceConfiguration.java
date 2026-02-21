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
package app.packed.namespace;

import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentHandle;

/**
 *
 */
public final class NamespaceConfiguration extends ComponentConfiguration {

    /** {@inheritDoc} */
    @Override
    protected ComponentHandle handle() {
        return null;
    }

    /** {@return whether or not this namespace is the root namespace in the application. */
    public boolean isRoot() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration tag(String... tags) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return null;
    }

}
