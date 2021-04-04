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

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.attribute.AttributeMap;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentScope;
import app.packed.component.ComponentStream;
import packed.internal.application.ApplicationLaunchContext;
import packed.internal.container.ExtensionSetup;
import packed.internal.invoke.constantpool.ConstantPool;
import packed.internal.util.ThrowableUtil;

/** An runtime representation of a component. */
// PackedComponentInstance
public final class PackedComponent implements Component {

    /** Any child components this component might have. Is null if we know the component will never have any children. */
    @Nullable
    private final Map<String, PackedComponent> children;

    /** The runtime model of the component. */
    final RuntimeComponentModel model;

    /** The name of the component. */
    private final String name;

    /** The parent component, or null if root. */
    @Nullable
    final PackedComponent parent;

    /** The region this component is part of. */
    public final ConstantPool pool;

    /**
     * Creates a new component.
     * 
     * @param parent
     *            the parent component, iff this component has a parent.
     * @param component
     *            the component build used to create this node
     * @param launch
     *            initialization context
     */
    public PackedComponent(@Nullable PackedComponent parent, ComponentSetup component, ApplicationLaunchContext launch) {
        this.parent = parent;
        this.model = RuntimeComponentModel.of(component);
        if (parent == null) {
            launch.component = this;
            this.name = launch.name;
        } else {
            this.name = requireNonNull(component.name);
        }

        // Vi opbygger structuren foerst...
        // Og saa initialisere vi ting bagefter
        // Structuren bliver noedt til at vide hvor den skal spoerge efter ting...
        Map<String, PackedComponent> children = null;
        LinkedHashMap<String, ComponentSetup> childComponents = component.children;
        if (childComponents != null) {
            // Maybe ordered is the default...
            LinkedHashMap<String, PackedComponent> result = new LinkedHashMap<>(childComponents.size());

            for (ComponentSetup cc : component.children.values()) {
                // We never carry over extensions into the runtime
                if (!(cc instanceof ExtensionSetup)) {
                    PackedComponent ac = new PackedComponent(this, cc, launch);
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

        // Cannot display the attribute values of /sds/we/ [source = wewe.class] until ccc.class has been instantiated

        // Vi create a new region is its the root, or if the component is a guest
        if (parent == null || component.modifiers().hasRuntime()) {
            this.pool = component.pool.newPool(launch);
            
            // Run all initializers
            for (MethodHandle mh : component.application.initializers) {
                try {
                    mh.invoke(pool);
                } catch (Throwable e) {
                    throw ThrowableUtil.orUndeclared(e);
                }
            }
        } else {
            this.pool = parent.pool;
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
        Map<String, PackedComponent> c = children;
        if (c == null) {
            return Collections.emptySet();
        }
        // Right now children are already immutable
        return Collections.unmodifiableCollection(c.values());
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return model.depth;
    }

    public Component findComponent(CharSequence path) {
        return findComponent(path.toString());
    }

    private PackedComponent findComponent(String path) {
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
        PackedComponent c = children.get(path);
        if (c == null) {
            String p = path.toString();
            String[] splits = p.split("/");
            Map<String, PackedComponent> chi = children;
            for (int i = 1; i < splits.length; i++) {
                if (chi == null) {
                    return null;
                }
                String ch = splits[i];
                PackedComponent ac = chi.get(ch);
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

    public boolean isInSameContainer(PackedComponent other) {
        return isInSameContainer0() == other.isInSameContainer0();
    }

    private PackedComponent isInSameContainer0() {
        PackedComponent c = this;
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
    public NamespacePath path() {
        return PackedTreePath.of(this);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentRelation relationTo(Component other) {
        requireNonNull(other, "other is null");
        return PackedComponentInstanceRelation.relation(this, (PackedComponent) other);
    }

    /**
     * @param path
     */
    @Override
    public Component resolve(CharSequence path) {
        Component c = findComponent(path);
        if (c == null) {
            // Maybe try an match with some fuzzy logic, if children is a resonable size)
            List<?> list = stream().map(e -> e.path()).toList();
            throw new IllegalArgumentException("Could not find component with path: " + path + " avilable components:" + list);
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public Component root() {
        PackedComponent c = this;
        PackedComponent p = parent;
        while (p != null) {
            c = p;
            p = p.parent;
        }
        return c;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(ComponentStream.Option... options) {
        return new PackedComponentStream(stream0(this, true, PackedComponentStreamOption.of(options)));
    }

    private Stream<Component> stream0(PackedComponent origin, boolean isRoot, PackedComponentStreamOption option) {
        // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
        Map<String, PackedComponent> c = children;
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
    public boolean isInSame(ComponentScope scope, Component other) {
        throw new UnsupportedOperationException();
    }
}
