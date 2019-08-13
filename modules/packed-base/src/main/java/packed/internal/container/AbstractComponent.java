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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.feature.FeatureMap;
import app.packed.util.Nullable;

/** An abstract base implementation of {@link Component}. */
abstract class AbstractComponent implements Component {

    /** Any child components this component might have. Is null if we know the component will never have any children. */
    @Nullable
    final Map<String, AbstractComponent> children;

    /** The configuration site of the component. */
    private final ConfigSite configSite;

    /** The depth of the component in a tree of components. */
    private final int depth;

    /** The description of this component (optional). */
    @Nullable
    private final String description;

    final FeatureMap features = new FeatureMap();

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
    AbstractComponent(@Nullable AbstractComponent parent, AbstractComponentConfiguration configuration, ArtifactInstantiationContext ic) {
        this.parent = parent;
        this.configSite = requireNonNull(configuration.configSite());
        this.description = configuration.getDescription();
        this.depth = configuration.depth();
        this.children = configuration.initializeChildren(this, ic);
        if (parent == null) {
            String n = configuration.name;
            ComponentNameWirelet ol = ic.wirelets().findLastOrNull(ComponentNameWirelet.class);
            if (ol != null) {
                n = ol.name;
                if (n.endsWith("?")) {
                    n = n.substring(0, n.length() - 1);
                }
            }
            this.name = n;
        } else {
            this.name = requireNonNull(configuration.name);
        }
        // for (FeatureKey<?> fk : configuration.features().keys()) {
        // Object o = configuration.features().get(fk);
        // }
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Component> children() {
        Map<String, AbstractComponent> c = children;
        if (c == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(c.values());
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
    public FeatureMap features() {
        return features;
    }

    public final Component findComponent(CharSequence path) {
        return findComponent(path.toString());
    }

    private Component findComponent(String path) {
        if (path.length() == 0) {
            throw new IllegalArgumentException("Cannot specify an empty (\"\") path");
        }
        if (path.charAt(0) == '/' && path.length() == 1) {
            if (path().toString().equals("/")) {
                return this;
            }
        }
        // Vi smider IllegalArgumentException hvis man absolute path, og man ikke har samme prefix....

        // TODO fix for non-absolute paths....
        //
        Component c = children.get(path);
        if (c == null) {
            String p = path.toString();
            String[] splits = p.split("/");
            Map<String, AbstractComponent> chi = children;
            for (int i = 1; i < splits.length; i++) {
                if (chi == null) {
                    return null;
                }
                String ch = splits[i];
                AbstractComponent ac = chi.get(ch);
                if (ac == null) {
                    return null;
                }
                if (i == splits.length - 1) {
                    return ac;
                }
                chi = ac.children;
            }
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public final String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        return PackedComponentPath.of(this);
    }

    @Override
    public final ComponentStream stream() {
        Map<String, AbstractComponent> c = children;
        if (c == null) {
            return new PackedComponentStream(Stream.of(this));
        }
        return new PackedComponentStream(Stream.concat(Stream.of(this), c.values().stream().flatMap(AbstractComponent::stream)));
    }

    /**
     * @param path
     */
    public final Component useComponent(CharSequence path) {
        Component c = findComponent(path);
        if (c == null) {
            // Maybe try an match with some fuzzy logic, if children is a resonable size)
            List<?> list = stream().map(e -> e.path()).collect(Collectors.toList());
            throw new IllegalArgumentException("Could not find component with path: " + path + " avilable components:" + list);
        }
        return c;
    }
}
