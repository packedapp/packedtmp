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
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.artifact.Host;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.Singleton;
import app.packed.component.Stateless;
import app.packed.component.feature.FeatureMap;
import app.packed.config.ConfigSite;
import app.packed.container.Container;
import app.packed.container.Extension;
import app.packed.lang.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.host.PackedGuestConfiguration;
import packed.internal.host.PackedHostConfiguration;

/**
 *
 */
public abstract class ComponentConfigurationToComponentAdaptor implements Component {

    /** A cached, lazy initialized list of all children. */
    private volatile Map<String, ComponentConfigurationToComponentAdaptor> children;

    /** The component configuration to wrap. */
    private final AbstractComponentConfiguration componentConfiguration;

    // Need to main any guest ancestor. As images must resolve in relation to it.
    private final PackedGuestConfiguration pgc;

    private ComponentConfigurationToComponentAdaptor(AbstractComponentConfiguration componentConfiguration, @Nullable PackedGuestConfiguration pgc) {
        this.componentConfiguration = requireNonNull(componentConfiguration);
        this.pgc = pgc;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final Collection<Component> children() {
        Map<String, ComponentConfigurationToComponentAdaptor> c = children;
        if (c == null) {
            if (componentConfiguration.children == null) {
                c = Map.of();
            } else {
                LinkedHashMap<String, ComponentConfigurationToComponentAdaptor> m = new LinkedHashMap<>();
                for (AbstractComponentConfiguration acc : componentConfiguration.children.values()) {
                    m.put(acc.name, of0(acc, pgc));
                }
                c = children = Map.copyOf(m);
            }
        }
        return (Collection) c.values();
    }

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configSite() {
        // For example, if image, should we
        return componentConfiguration.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        return componentConfiguration.depth();
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(componentConfiguration.getDescription());
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<Class<? extends Extension>> extension() {
        return componentConfiguration.extension();
    }

    /** {@inheritDoc} */
    @Override
    public final FeatureMap features() {
        // TODO we need to be able to freeze this for images
        return componentConfiguration.features();
    }

    /** {@inheritDoc} */
    @Override
    public final String name() {
        return componentConfiguration.getName();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        if (pgc != null) {
            return pgc.path().add(componentConfiguration.path());
        }
        return componentConfiguration.path();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentStream stream(ComponentStream.Option... options) {
        return new PackedComponentStream(stream0(componentConfiguration, true, PackedComponentStreamOption.of(options)));
    }

    private final Stream<Component> stream0(AbstractComponentConfiguration origin, boolean isRoot, PackedComponentStreamOption option) {
        // Also fix in ComponentConfigurationToComponentAdaptor when changing stuff here
        children(); // lazy calc
        Map<String, ComponentConfigurationToComponentAdaptor> c = children;
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

    /** {@inheritDoc} */
    @Override
    public final void traverse(Consumer<? super Component> action) {
        throw new UnsupportedOperationException();
    }

    public static Container of(PackedContainerConfiguration pcc) {
        return (Container) of0(pcc, null);
    }

    private static ComponentConfigurationToComponentAdaptor of0(ComponentConfiguration bcc, @Nullable PackedGuestConfiguration pgc) {
        if (bcc instanceof PackedContainerConfiguration) {
            return new ContainerAdaptor((PackedContainerConfiguration) bcc, pgc);
        } else if (bcc instanceof PackedStatelessComponentConfiguration) {
            return new StatelessAdaptor((PackedStatelessComponentConfiguration) bcc, pgc);
        } else if (bcc instanceof PackedSingletonConfiguration) {
            return new SingleAdaptor((PackedSingletonConfiguration<?>) bcc, pgc);
        } else if (bcc instanceof PackedHostConfiguration) {
            return new HostAdaptor((PackedHostConfiguration) bcc, pgc);
        } else if (bcc instanceof PackedGuestConfiguration) {
            // Need to figure out hosts on hosts..
            PackedGuestConfiguration pgcc = (PackedGuestConfiguration) bcc;
            return new ContainerAdaptor(pgcc.delegate, pgcc);
        } else {
            // TODO add host, when we get a configuration class
            throw new IllegalArgumentException("Unknown configuration type, type = " + bcc);
        }
    }

    private final static class ContainerAdaptor extends ComponentConfigurationToComponentAdaptor implements Container {

        public ContainerAdaptor(PackedContainerConfiguration pcc, @Nullable PackedGuestConfiguration pgc) {
            super(pcc, pgc);
        }
    }

    private final static class SingleAdaptor extends ComponentConfigurationToComponentAdaptor implements Singleton {

        public SingleAdaptor(PackedSingletonConfiguration<?> conf, @Nullable PackedGuestConfiguration pgc) {
            super(conf, pgc);
        }
    }

    private final static class StatelessAdaptor extends ComponentConfigurationToComponentAdaptor implements Stateless {

        public StatelessAdaptor(PackedStatelessComponentConfiguration conf, @Nullable PackedGuestConfiguration pgc) {
            super(conf, pgc);
        }
    }

    private final static class HostAdaptor extends ComponentConfigurationToComponentAdaptor implements Host {

        public HostAdaptor(PackedHostConfiguration conf, @Nullable PackedGuestConfiguration pgc) {
            super(conf, pgc);
        }
    }

}
