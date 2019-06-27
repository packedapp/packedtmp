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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.util.Nullable;

/** An abstract base implementation of {@link Component}. */
abstract class AbstractComponent implements Component {

    /** The configuration site of the component. */
    private final ConfigSite configSite;

    /** The depth of the component in a tree of components. */
    private final int depth;

    /** The description of this component (optional). */
    @Nullable
    private final String description;

    /** The name of the component. The name is guaranteed to be unique between siblings. */
    // TODO I think we need to remove final. Problem is with Host. Where we putIfAbsent.
    // There is a small window where it might have been overridden....
    // Unless we create the container in computeIfAbsent.... which I just think we should....
    // Problem is also that the final name might not be stored in AbstractComponentConfiguration...
    //// Auch I think we need to maintain some of that naming state for images....
    /// For example, whether or not naming is free...
    private final String name;

    /** The parent component, iff this component has a parent. */
    @Nullable
    final AbstractComponent parent;

    /**
     * Creates a new abstract component.
     * 
     * @param parent
     *            the parent component, iff this component has a parent.
     * @param configuration
     *            the configuration used for creating this component
     */
    AbstractComponent(@Nullable AbstractComponent parent, AbstractComponentConfiguration configuration) {
        this.parent = parent;
        this.configSite = requireNonNull(configuration.configSite());
        this.description = configuration.getDescription();
        this.name = requireNonNull(configuration.name);
        this.depth = configuration.depth();
    }

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configSite() {
        return configSite;
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        return depth;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /** {@inheritDoc} */
    @Override
    public final String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        return new InternalComponentPath(this);
    }
}
