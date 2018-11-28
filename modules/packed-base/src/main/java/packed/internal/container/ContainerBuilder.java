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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import app.packed.bundle.Bundle;
import app.packed.bundle.ContainerBundle;
import app.packed.bundle.ImportExportStage;
import app.packed.container.ComponentConfiguration;
import app.packed.container.Container;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.TypeLiteral;
import app.packed.util.Nullable;
import packed.internal.inject.builder.InjectorBuilder;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A builder of {@link Container containers}. Is both used via {@link ContainerBundle} and
 * {@link ContainerConfiguration}.
 */
public final class ContainerBuilder extends InjectorBuilder implements ContainerConfiguration {

    /** The name of the container, or null if no name has been set. */
    @Nullable
    private String name;

    /** The root component, or null if no root component has been set. */
    @Nullable
    private InternalComponentConfiguration<?> root;

    public ContainerBuilder(InternalConfigurationSite configurationSite) {
        super(configurationSite);
    }

    public ContainerBuilder(InternalConfigurationSite configurationSite, ContainerBundle bundle) {
        super(configurationSite, bundle);
    }

    @Override
    public Container build() {

        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public <T> ComponentConfiguration<T> install(Class<T> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public <T> ComponentConfiguration<T> install(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        return install0(InternalFactory.from(factory));
    }

    /** {@inheritDoc} */
    @Override
    public <T> ComponentConfiguration<T> install(T instance) {
        checkConfigurable();
        return install0(
                new InternalComponentConfiguration<T>(this, getConfigurationSite().spawnStack(ConfigurationSiteType.COMPONENT_INSTALL), root, instance));
    }

    /** {@inheritDoc} */
    @Override
    public <T> ComponentConfiguration<T> install(TypeLiteral<T> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    /**
     * Sets the component root iff a root has not already been set.
     *
     * @param configuration
     *            the component configuration
     * @return the specified component configuration
     */
    private <T> InternalComponentConfiguration<T> install0(InternalComponentConfiguration<T> configuration) {
        if (root == null) {
            root = configuration;
        }
        return configuration;
    }

    private <T> InternalComponentConfiguration<T> install0(InternalFactory<T> factory) {
        InternalFactory<T> f = factory;
        f = accessor.readable(f);
        InternalComponentConfiguration<T> icc = new InternalComponentConfiguration<T>(this,
                getConfigurationSite().spawnStack(ConfigurationSiteType.COMPONENT_INSTALL), root, f);
        bindNode(icc).as(f.getKey());
        return install0(icc);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerBuilder setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /** Small for utility class for generate a best effort unique name for containers. */
    static class InternalContainerName {

        /** Assigns unique IDs, starting with 1 when lazy naming containers. */
        private static final AtomicLong ANONYMOUS_ID = new AtomicLong();

        private static final ClassValue<Supplier<String>> BUNDLE_NAME_SUPPLIER = new ClassValue<>() {
            private final AtomicLong L = new AtomicLong();

            @Override
            protected Supplier<String> computeValue(Class<?> type) {
                String simpleName = type.getSimpleName();
                String s = simpleName.endsWith("Bundle") && simpleName.length() > 6 ? simpleName.substring(simpleName.length() - 6) : simpleName;
                return () -> s + L.incrementAndGet();
            }
        };

        static String fromBundleType(Class<? extends Bundle> cl) {
            return BUNDLE_NAME_SUPPLIER.get(cl).get();
        }

        static String next() {
            return "Container" + ANONYMOUS_ID.incrementAndGet();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void containerInstall(Class<? extends ContainerBundle> bundleType, ImportExportStage... filters) {}

    /** {@inheritDoc} */
    @Override
    public void containerInstall(ContainerBundle bundle, ImportExportStage... filters) {}
}
