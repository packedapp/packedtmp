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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.attribute.AttributeSet;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRelation;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;

/**
 *
 */

// TODO maaske kan configuration implementere Component nu naar den ikke er offentlig????
// Men det er context jo...
public final class ComponentAdaptor implements Component {

    /** A cached, lazy initialized list of all children. */
    private volatile Map<String, ComponentAdaptor> children;

    /** The component configuration to wrap. */
    public final PackedComponentConfigurationContext componentConfiguration;

    // Need to main any guest ancestor. As images must resolve in relation to it.
    // private final List<PackedGuestConfigurationContext> pgc;

    private ComponentAdaptor(PackedComponentConfigurationContext componentConfiguration /* , List<PackedGuestConfigurationContext> pgc */) {
        this.componentConfiguration = requireNonNull(componentConfiguration);
        // this.pgc = pgc;
    }

    /** {@inheritDoc} */
    @Override
    public AttributeSet attributes() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final Collection<Component> children() {
        Map<String, ComponentAdaptor> c = children;

        // TODO fix this shit
//        return new AbstractCollection<Component>() {
//
//            @Override
//            public Iterator<Component> iterator() {
//                Map<String, ComponentConfigurationToComponentAdaptor> c = children;
//                // TODO make view instead
//                return c == null ? List.<Component>of().iterator() : null;
//            }
//
//            @Override
//            public int size() {
//                Map<String, ComponentConfigurationToComponentAdaptor> c = children;
//                return c == null ? 0 : c.size();
//            }
//        };

        if (c == null) {
            if (componentConfiguration.firstChild == null) {
                // It is not really a view...
                // return new AbstractCollection<>(); <--- cache it,
                c = Map.of();
            } else {
                LinkedHashMap<String, ComponentAdaptor> m = new LinkedHashMap<>();
                for (PackedComponentConfigurationContext acc = componentConfiguration.firstChild; acc != null; acc = acc.nextSibling) {
                    m.put(acc.name, of(acc /* , pgc */));
                }
                c = children = Map.copyOf(m);
            }
        }
        return (Collection) c.values();
    }

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configSite() {
        // We might need to rewrite this for image...
        return componentConfiguration.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        int depth = componentConfiguration.depth;
        /*
         * for (PackedGuestConfigurationContext p : pgc) { depth += p.depth; }
         */
        return depth;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(componentConfiguration.getDescription());
    }

    public final Optional<Class<? extends Extension>> extension() {
        return componentConfiguration.extension();
    }

    /** {@inheritDoc} */
    @Override
    public final String name() {
        return componentConfiguration.getName();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Component> parent() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        ComponentPath cp = componentConfiguration.path();
//        for (PackedGuestConfigurationContext p : pgc) {
//            cp = p.path().add(cp);
//        }
        return cp;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentRelation relationTo(Component other) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentStream stream(ComponentStream.Option... options) {
        return new PackedComponentStream(stream0(componentConfiguration, true, PackedComponentStreamOption.of(options)));
    }

    private final Stream<Component> stream0(PackedComponentConfigurationContext origin, boolean isRoot, PackedComponentStreamOption option) {
        // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
        children(); // lazy calc
        Map<String, ComponentAdaptor> c = children;
        if (c != null && !c.isEmpty()) {
            if (option.processThisDeeper(origin, componentConfiguration)) {
                Stream<Component> s = c.values().stream().flatMap(co -> co.stream0(origin, false, option));
                return isRoot && option.excludeOrigin() ? s : Stream.concat(Stream.of(this), s);
            }
            return Stream.empty();
        } else {
            return isRoot && option.excludeOrigin() ? Stream.empty() : Stream.of(this);
        }
    }

    public static ComponentAdaptor of(PackedComponentConfigurationContext bcc /* , List<PackedGuestConfigurationContext> pgc */) {
        return new ComponentAdaptor(bcc /* , pgc */);
//        
//        if (bcc instanceof PackedGuestConfigurationContext) {
//            // Need to figure out hosts on hosts..
//            PackedGuestConfigurationContext pgcc = (PackedGuestConfigurationContext) bcc;
//            LinkedList<PackedGuestConfigurationContext> al = new LinkedList<>(pgc);
//            al.addFirst(pgcc);
//            return new ComponentConfigurationToComponentAdaptor(pgcc.delegate, List.copyOf(al));
//        } else {
//            return new ComponentConfigurationToComponentAdaptor(bcc, pgc);
//        }
    }
}
