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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.base.AttributeMap;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;

/** An runtime representation of a component. */
public final class ComponentNode implements Component {

    /** Any child components this component might have. Is null if we know the component will never have any children. */
    @Nullable
    private final Map<String, ComponentNode> children;

    /** The runtime model of the component. */
    final RuntimeComponentModel model;

    /** The name of the component. The name is guaranteed to be unique between siblings. */
    private final String name;

    /** The parent component if it has one. */
    @Nullable
    final ComponentNode parent; // Parent is always stored as the first object in NodeStore...

    /** The region this component is part of. */
    final Region region;

    /**
     * Creates a new component node.
     * 
     * @param parent
     *            the parent component, iff this component has a parent.
     * @param configuration
     *            the configuration used to create this node
     */
    ComponentNode(@Nullable ComponentNode parent, ComponentNodeConfiguration configuration, PackedInitializationContext pic) {
        this.parent = parent;
        this.model = RuntimeComponentModel.of(configuration);
        if (parent == null) {
            this.name = pic.rootName(configuration);
        } else {
            this.name = requireNonNull(configuration.name);
        }

        // Vi opbygger structuren foerst...
        // Og saa initialisere vi ting bagefter
        // Structuren bliver noedt til at vide hvor den skal spoerge efter ting...
        Map<String, ComponentNode> children = null;
        if (configuration.treeFirstChild != null) {
            // Maybe ordered is the default...
            LinkedHashMap<String, ComponentNode> result = new LinkedHashMap<>(configuration.treeChildren.size());

            for (ComponentNodeConfiguration cc = configuration.treeFirstChild; cc != null; cc = cc.treeNextSibling) {
                if (!cc.driver().modifiers().isExtension()) {
                    ComponentNode ac = new ComponentNode(this, cc, pic);
                    result.put(ac.name(), ac);
                }
            }

            children = Map.copyOf(result);
        }
        this.children = children;

        // Maaske er region ikke final...
        // Problemet er en bruger der injecter Component i constructuren.
        // Og saa kalder children(). Som jo af gode grunde ikke noedvendig er fuldt
        // initialiseret f.eks. hvis vi supporter Source attributer...
        // Alternativ fejler vi bare naar folk kalder source attributer...
        // Tror bare vi har et check om en source instance er non-null

        // Vi create a new region is its the root, or if the component is a guest
        if (parent == null || configuration.modifiers().isGuest()) {
            this.region = configuration.region.newRegion(pic, this);
        } else {
            this.region = parent.region;
        }

    }

    /** {@inheritDoc} */
    @Override
    public AttributeMap attributes() {
        return AttributeMap.of();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Component> children() {
        Map<String, ComponentNode> c = children;
        if (c == null) {
            return Collections.emptySet();
        }
        // Right now children are already immutable
        return Collections.unmodifiableCollection(c.values());
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return model.configSite;
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return model.depth;
    }

    public Component findComponent(CharSequence path) {
        return findComponent(path.toString());
    }

    private ComponentNode findComponent(String path) {
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
        ComponentNode c = children.get(path);
        if (c == null) {
            String p = path.toString();
            String[] splits = p.split("/");
            Map<String, ComponentNode> chi = children;
            for (int i = 1; i < splits.length; i++) {
                if (chi == null) {
                    return null;
                }
                String ch = splits[i];
                ComponentNode ac = chi.get(ch);
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
    public boolean hasModifier(ComponentModifier modifier) {
        return PackedComponentModifierSet.isSet(model.modifiers, modifier);
    }

    public boolean isInSameContainer(ComponentNode other) {
        return isInSameContainer0() == other.isInSameContainer0();
    }

    private ComponentNode isInSameContainer0() {
        ComponentNode c = this;
        while (!(c.model.isContainer())) {
            c = c.parent;
        }
        return c;
    }

    public RuntimeComponentModel model() {
        return model;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentModifierSet modifiers() {
        return new PackedComponentModifierSet(model.modifiers);
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> parent() {
        return Optional.ofNullable(parent);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return PackedComponentPath.of(this);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentRelation relationTo(Component other) {
        return PackedComponentRelation.find(this, other);
    }

    /**
     * @param path
     */
    @Override
    public Component resolve(CharSequence path) {
        Component c = findComponent(path);
        if (c == null) {
            // Maybe try an match with some fuzzy logic, if children is a resonable size)
            List<?> list = stream().map(e -> e.path()).collect(Collectors.toList());
            throw new IllegalArgumentException("Could not find component with path: " + path + " avilable components:" + list);
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(ComponentStream.Option... options) {
        return new PackedComponentStream(stream0(this, true, PackedComponentStreamOption.of(options)));
    }

    private Stream<Component> stream0(ComponentNode origin, boolean isRoot, PackedComponentStreamOption option) {
        // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
        Map<String, ComponentNode> c = children;
        if (c != null && !c.isEmpty()) {
            if (option.processThisDeeper(origin, this)) {
                Stream<Component> s = c.values().stream().flatMap(co -> co.stream0(origin, false, option));
                return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
            }
            return Stream.empty();
        } else {
            return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
        }
    }

    @Override
    public Component system() {
        ComponentNode p = parent;
        while (p.parent != null) {
            p = p.parent;
        }
        return p;
    }
}
