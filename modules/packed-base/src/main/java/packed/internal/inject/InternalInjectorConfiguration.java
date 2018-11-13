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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Consumer;

import app.packed.inject.BindingMode;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceStagingArea;
import app.packed.inject.TypeLiteral;
import packed.internal.inject.buildnodes.BuildNode;
import packed.internal.inject.buildnodes.BuildNodeFactoryPrototype;
import packed.internal.inject.buildnodes.BuildNodeFactorySingleton;
import packed.internal.inject.buildnodes.BuildNodeInstance;
import packed.internal.inject.buildnodes.ImportServicesFromInjector;
import packed.internal.inject.buildnodes.InjectorBuilder;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.invokers.LookupDescriptorAccessor;
import packed.internal.util.AbstractConfiguration;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** The default implementation of {@link InjectorConfiguration}. */
public class InternalInjectorConfiguration extends AbstractConfiguration<InternalInjectorConfiguration> implements InjectorConfiguration {

    /** The lookup object. We default to public access */
    protected LookupDescriptorAccessor accessor = LookupDescriptorAccessor.PUBLIC;

    public final InjectorBuilder builder = new InjectorBuilder(this);

    InternalInjector parentInjector;

    /** The configuration of the injector */
    protected final InternalConfigurationSite configurationSite;

    /**
     * Creates a new configuration.
     * 
     * @param configurationSite
     *            the configuration site
     */
    public InternalInjectorConfiguration(InternalConfigurationSite configurationSite) {
        this.configurationSite = requireNonNull(configurationSite);
    }

    private <T> BuildNode<T> add(BuildNode<T> node) {
        // WHEN YOU CALL THIS METHOD, remember the key is not automatically bound, but must use .as(xxxxx)
        builder.addAndScan(node);
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.EAGER_SINGLETON, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.EAGER_SINGLETON, factory);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T> ServiceConfiguration<T> bind(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return add(new BuildNodeInstance<>(this, configurationSite.spawnStack(ConfigurationSiteType.INJECTOR_BIND), instance)).as((Class) instance.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.EAGER_SINGLETON, Factory.findInjectable(implementation));
    }

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.LAZY_SINGLETON, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceConfiguration<T> bindLazy(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.LAZY_SINGLETON, factory);
    }

    @Override
    public final <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.LAZY_SINGLETON, Factory.findInjectable(implementation));
    }

    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.PROTOTYPE, factory);
    }

    @Override
    public final <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        checkConfigurable();
        // int depth = depth();
        // ConfigurationSite point = depth == 0 ? ConfigurationSite.NO_INFO : ConfigurationSite.fromFrame(W.walk(e ->
        // e.skip(depth).findFirst()));
        return fromFactory(BindingMode.PROTOTYPE, Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    protected int depth() {
        return 1;
    }

    protected final <T> ServiceConfiguration<T> fromFactory(BindingMode mode, Factory<T> factory) {
        InternalConfigurationSite frame = configurationSite.spawnStack(ConfigurationSiteType.INJECTOR_BIND);
        final BuildNode<T> node;
        InternalFactory<T> f = InternalFactory.from(factory);
        f = accessor.readable(f);
        switch (mode) {
        case LAZY_SINGLETON:
            node = new BuildNodeFactorySingleton<>(this, frame, f, true);
            break;
        case EAGER_SINGLETON:
            node = new BuildNodeFactorySingleton<>(this, frame, f, false);
            break;
        default:
            node = new BuildNodeFactoryPrototype<T>(this, frame, f);
        }
        return add(node).as(factory.getKey());
    }

    @Override
    public final void importServicesFrom(Injector injector) {
        importServices(injector, null, true);
    }

    /** {@inheritDoc} */
    @Override
    public final void importServicesFrom(Injector injector, Consumer<? super ServiceStagingArea> configurator) {
        requireNonNull(configurator, "configurator is null");
        importServices(injector, configurator, false);
    }

    /** {@inheritDoc} */
    private void importServices(Injector injector, Consumer<? super ServiceStagingArea> c, boolean autoImport) {
        requireNonNull(injector, "injector is null");
        checkConfigurable();
        InternalConfigurationSite cs = configurationSite.spawnStack(ConfigurationSiteType.INJECTOR_IMPORT_FROM);
        ImportServicesFromInjector ifi = new ImportServicesFromInjector(this, injector, cs);
        builder.addImportInjector(ifi);
        if (autoImport) {
            ifi.importAllServices();
        } else {
            c.accept(ifi);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null, use MethodHandles.publicLookup() to set public access");
        checkConfigurable();
        accessor = LookupDescriptorAccessor.get(lookup);
    }
}
