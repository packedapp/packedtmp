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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.container.extension.Extension;
import app.packed.container.extension.feature.FeatureMap;

/**
 *
 */
// Tvivler paa vi beholder ArtifactImage.stream()
public final class ComponentConfigurationToComponentAdaptor implements Component {

    private volatile List<ComponentConfigurationToComponentAdaptor> children;

    private final AbstractComponentConfiguration<?> componentConfiguration;

    public ComponentConfigurationToComponentAdaptor(AbstractComponentConfiguration<?> componentConfiguration) {
        this.componentConfiguration = requireNonNull(componentConfiguration);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Collection<Component> children() {
        List<ComponentConfigurationToComponentAdaptor> c = children;
        if (c == null) {
            if (componentConfiguration.children == null) {
                c = List.of();
            } else {
                ArrayList<ComponentConfigurationToComponentAdaptor> tmp = new ArrayList<>();
                for (AbstractComponentConfiguration<?> acc : componentConfiguration.children.values()) {
                    tmp.add(new ComponentConfigurationToComponentAdaptor(acc));
                }
                c = children = List.copyOf(tmp);
            }
        }
        return (Collection) c;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return componentConfiguration.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return componentConfiguration.depth();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return Optional.ofNullable(componentConfiguration.getDescription());
    }

    /** {@inheritDoc} */
    @Override
    public FeatureMap features() {
        // TODO we need to be able to freeze this for images
        return componentConfiguration.features();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return componentConfiguration.getName();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return componentConfiguration.path();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream() {
        children();
        List<ComponentConfigurationToComponentAdaptor> c = children;
        if (c == null) {
            return new PackedComponentStream(Stream.of(this));
        }
        return new PackedComponentStream(Stream.concat(Stream.of(this), c.stream().flatMap(ComponentConfigurationToComponentAdaptor::stream)));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Extension>> extension() {
        return componentConfiguration.extension();
    }

}
