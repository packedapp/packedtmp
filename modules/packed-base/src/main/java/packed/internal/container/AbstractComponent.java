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

import java.util.Optional;

import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.util.Nullable;

/**
 *
 */
abstract class AbstractComponent {

    /** The configuration site of the component. */
    private final ConfigSite configurationSite;

    /** The description of this component (optional). */
    @Nullable
    private final String description;

    /** The name of the component. The name is unique among other siblings. */
    private final String name;

    /** The container in which this component lives. */
    final AbstractComponent parent;

    AbstractComponent(AbstractComponent parent, AbstractComponentConfiguration configuration) {
        this.configurationSite = configuration.configurationSite();
        this.parent = parent;
        this.description = configuration.getDescription();
        this.name = configuration.name;
    }

    public final ConfigSite configurationSite() {
        return configurationSite;
    }

    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public final String name() {
        return name;
    }

    public final ComponentPath path() {
        return new InternalComponentPath(this);
    }
}
