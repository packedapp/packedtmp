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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.attribute.AttributeSet;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;

/** An adaptor of the {@link Component} interface from a {@link ComponentNodeConfiguration}. */
public final class ComponentAdaptor implements Component {

    /** A cached, lazy initialized list of all children. */
    private volatile Map<String, ComponentAdaptor> children;

    /** The component configuration to wrap. */
    public final ComponentNodeConfiguration conf;

    private ComponentAdaptor(ComponentNodeConfiguration c) {
        this.conf = requireNonNull(c);
    }

    /** {@inheritDoc} */
    @Override
    public AttributeSet attributes() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Component> children() {
        int size = children == null ? 0 : children.size();
        if (size == 0) {
            return List.of();
        } else {
            ArrayList<Component> result = new ArrayList<>(size);
            for (ComponentNodeConfiguration acc = conf.firstChild; acc != null; acc = acc.nextSibling) {
                result.add(of(acc));
            }
            return result;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configSite() {
        return conf.configSite(); // We might need to rewrite this for image...
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        return conf.depth;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(conf.getDescription());
    }

    /** {@inheritDoc} */
    @Override
    public final String name() {
        return conf.getName();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> parent() {
        ComponentNodeConfiguration p = conf.parent;
        return p == null ? Optional.empty() : Optional.of(of(p));
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        ComponentPath cp = conf.path();
//        for (PackedGuestConfigurationContext p : pgc) {
//            cp = p.path().add(cp);
//        }
        return cp;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentRelation relationTo(Component other) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentStream stream(ComponentStream.Option... options) {
        return new PackedComponentStream(stream0(conf, true, PackedComponentStreamOption.of(options)));
    }

    private final Stream<Component> stream0(ComponentNodeConfiguration origin, boolean isRoot, PackedComponentStreamOption option) {
        // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
        children(); // lazy calc
        Map<String, ComponentAdaptor> c = children;
        if (c != null && !c.isEmpty()) {
            if (option.processThisDeeper(origin, this.conf)) {
                Stream<Component> s = c.values().stream().flatMap(co -> co.stream0(origin, false, option));
                return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
            }
            return Stream.empty();
        } else {
            return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
        }
    }

    public static ComponentAdaptor of(ComponentNodeConfiguration bcc) {
        return new ComponentAdaptor(bcc);
    }
}
