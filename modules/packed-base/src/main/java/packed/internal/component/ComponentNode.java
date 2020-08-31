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
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.container.PackedContainerRole;
import packed.internal.container.PackedExtensionConfiguration;
import packed.internal.lifecycle.PackedInitializationContext;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.runtime.PackedInjector;

/** An runtime representation of a component. */
public final class ComponentNode implements Component {

    /** Any child components this component might have. Is null if we know the component will never have any children. */
    @Nullable
    private final Map<String, ComponentNode> children;

    public final Object[] data = new Object[1];

    /** The runtime model of the component. */
    final RuntimeComponentModel model;

    /** The name of the component. The name is guaranteed to be unique between siblings. */
    private final String name;

    /** The parent component if it has one. */
    @Nullable
    final ComponentNode parent;

    /** The index into the pod. */
    final int storeOffset = 0;

    /** The pod the component is a part of, components that are strongly connected are all in the same pod. */
    final NodeStore store;

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
        this.store = requireNonNull(configuration.guest.pod());

        // Initialize name, we don't want to override this in Configuration context. We don't want the conf to change if
        // image...
        // Check for any runtime wirelets that have been specified.
        // This is probably not the right way to do it. Especially with hosts.. Fix it when we get to hosts...
        // Maybe this can be written in PodInstantiationContext
        if (parent == null) {
            this.name = pic.rootName(configuration);
        } else {
            this.name = requireNonNull(configuration.name);
        }

        // Initialize Container
        if (configuration.driver().modifiers().isContainer()) {
            // I think this injector is only available for the top of an assembly
            PackedContainerRole container = configuration.container;
            Injector i = null;

            if (container.extensions != null) {
                PackedExtensionConfiguration ee = container.extensions.get(ServiceExtension.class);
                if (ee != null) {
                    i = ServiceExtensionNode.fromExtension(((ServiceExtension) ee.instance())).onInstantiate(pic.wirelets());
                }
            }
            if (i == null) {
                i = new PackedInjector(configuration.configSite(), new LinkedHashMap<>());
            }

            data[0] = i;
        }

        // Last but least, initialize all children...

        Map<String, ComponentNode> c = null;
        if (configuration.firstChild != null) {
            // Maybe ordered is the default...
            LinkedHashMap<String, ComponentNode> result = new LinkedHashMap<>(configuration.children.size());

            for (ComponentNodeConfiguration cc = configuration.firstChild; cc != null; cc = cc.nextSibling) {
                if (!cc.driver().modifiers().isExtension()) {
                    ComponentNode ac = new ComponentNode(this, cc, pic);
                    result.put(ac.name(), ac);
                }
            }

            c = Map.copyOf(result);
        }
        this.children = c;
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
    public boolean hasModifier(ComponentModifier property) {
        return PackedComponentModifierSet.isPropertySet(model.properties, property);
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
        return new PackedComponentModifierSet(model.properties);
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
