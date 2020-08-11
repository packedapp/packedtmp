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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.container.ComponentWirelet.ComponentNameWirelet;
import packed.internal.container.PackedContainer;

/** An default implementation of {@link Component}. */
public class PackedComponent implements Component {

    /** Any child components this component might have. Is null if we know the component will never have any children. */
    @Nullable
    public final Map<String, PackedComponent> children;

    /** The runtime model of the component. */
    final RuntimeComponentModel model;

    /** The name of the component. The name is guaranteed to be unique between siblings. */
    final String name;

    /** The parent component, iff this component has a parent. */
    @Nullable
    final PackedComponent parent;

    /** The pod the component is a part of, components that are strongly connected are all in the same pod. */
    final PackedPod pod;

    /**
     * Creates a new abstract component.
     * 
     * @param parent
     *            the parent component, iff this component has a parent.
     * @param context
     *            the configuration used for creating this component
     */
    public PackedComponent(@Nullable PackedComponent parent, PackedComponentConfigurationContext context, PackedInstantiationContext ic) {
        this.parent = parent;
        this.pod = context.pod.pod();
        this.model = requireNonNull(context.descritor());

        // Initialize name
        if (parent == null) {
            String n = context.name;
            ComponentNameWirelet ol = ic.wirelets() == null ? null : ic.wirelets().nameWirelet();
            if (ol != null) {
                n = ol.name;
                if (n.endsWith("?")) {
                    n = n.substring(0, n.length() - 1);
                }
            }
            this.name = n;
        } else {
            this.name = requireNonNull(context.name);
        }

        // Last but least, initialize all children...
        this.children = context.initializeChildren(this, ic);
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Component> children() {
        Map<String, PackedComponent> c = children;
        if (c == null) {
            return Collections.emptySet();
        }
        // Right now it is actually immutable
        return Collections.unmodifiableCollection(c.values());
    }

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configSite() {
        return model.configSite;
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        return model.depth;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(model.description);
    }

    public final Component findComponent(CharSequence path) {
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
        //
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

    public boolean isInSameContainer(PackedComponent other) {
        return isInSameContainer0() == other.isInSameContainer0();
    }

    private PackedContainer isInSameContainer0() {
        PackedComponent c = this;
        while (!(c instanceof PackedContainer)) {
            c = c.parent;
        }
        return (PackedContainer) c;
    }

    public RuntimeComponentModel model() {
        return model;
    }

    /** {@inheritDoc} */
    @Override
    public final String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<Component> parent() {
        return Optional.ofNullable(parent);
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        return PackedComponentPath.of(this);
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentRelation relationTo(Component other) {
        return PackedComponentRelation.find(this, other);
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentStream stream(ComponentStream.Option... options) {
        return new PackedComponentStream(stream0(this, true, PackedComponentStreamOption.of(options)));
    }

    private final Stream<Component> stream0(PackedComponent origin, boolean isRoot, PackedComponentStreamOption option) {
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
