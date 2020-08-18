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
package app.packed.component;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.container.BaseBundle;
import app.packed.service.ExportedServiceConfiguration;
import app.packed.service.ServiceExtension;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedComponentDriver.SingletonComponentDriver;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionNode;

/**
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseBundle}.
 * <p>
 * It it also possible to install components at runtime via {@link Component}.
 */
public class SingletonConfiguration<T> extends AbstractComponentConfiguration {

    public final ComponentNodeConfiguration node;

    private BuildEntry<T> buildEntry;

    public SingletonConfiguration(ComponentConfigurationContext node) {
        super(node);
        this.node = (ComponentNodeConfiguration) node;
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Key)
     */
    public SingletonConfiguration<T> as(Class<? super T> key) {
        return as(Key.of(key));
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Class)
     */
    public SingletonConfiguration<T> as(Key<? super T> key) {
        checkConfigurable();
        entry().as(key);
        return this;
    }

    @SuppressWarnings("unchecked")
    BuildEntry<T> entry() {
        if (buildEntry == null) {
            ServiceExtension e = node.container().use(ServiceExtension.class);
            ServiceExtensionNode sen = ServiceExtensionNode.fromExtension(e);
            SingletonComponentDriver<T> scd = (SingletonComponentDriver<T>) node.driver();
            if (scd.instance != null) {
                buildEntry = sen.provider().provideInstance(node, scd.instance);
            } else {
                buildEntry = sen.provider().provideFactory(node);
            }
        }
        return buildEntry;
    }

    public Optional<Key<?>> key() {
        return buildEntry == null ? Optional.empty() : Optional.of(buildEntry.key());
    }

    public SingletonConfiguration<T> provide() {
        entry();
        // TODO should provide as the default key
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SingletonConfiguration<T> setName(String name) {
        node.setName(name);
        return this;
    }

    public ExportedServiceConfiguration<T> export() {
        checkConfigurable();
        return buildEntry.node.exports().export(buildEntry, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    public static <T> InstanceSourcedDriver<SingletonConfiguration<T>, T> driver() {
        return PackedComponentDriver.SingletonComponentDriver.driver();
    }
}
