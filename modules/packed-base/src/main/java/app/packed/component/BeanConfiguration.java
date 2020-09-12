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

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.component.ComponentDriver.Option;
import app.packed.container.BaseBundle;
import app.packed.service.ExportedServiceConfiguration;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.service.buildtime.BuildtimeService;

/**
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseBundle}.
 * <p>
 * It it also possible to install components at runtime via {@link Component}.
 */
@SuppressWarnings("exports")
public class BeanConfiguration<T> extends AbstractComponentConfiguration {

    @SuppressWarnings("rawtypes")
    private static final InstanceComponentDriver ICD = InstanceComponentDriver.of(MethodHandles.lookup(), BeanConfiguration.class, Option.constantSource());

    public final ComponentNodeConfiguration component;

    private BuildtimeService<T> buildEntry;

    public BeanConfiguration(ComponentConfigurationContext context) {
        super(context);
        this.component = (ComponentNodeConfiguration) context;
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
    public BeanConfiguration<T> as(Class<? super T> key) {
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
    public BeanConfiguration<T> as(Key<? super T> key) {
        checkConfigurable();
        provide().as(key);
        return this;
    }

    public Optional<Key<?>> key() {
        return buildEntry == null ? Optional.empty() : Optional.of(buildEntry.key());
    }

    @SuppressWarnings("unchecked")
    public BeanConfiguration<T> provide() {
        if (buildEntry == null) {
            buildEntry = (BuildtimeService<T>) component.source.provide();
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration<T> setName(String name) {
        component.setName(name);
        return this;
    }

    public ExportedServiceConfiguration<T> exportAs(Class<? super T> key) {
        return export().as(key);
    }

    // Once a bean has been exported, its key cannot be changed...
    public ExportedServiceConfiguration<T> export() {
        checkConfigurable();
        provide();
        // buildEntry might not have been set yet...
        return buildEntry.im.exports().export(buildEntry, captureStackFrame(ConfigSiteInjectOperations.INJECTOR_EXPORT_SERVICE));
    }

    @SuppressWarnings("unchecked")
    public static <T> InstanceComponentDriver<BeanConfiguration<T>, T> driver() {
        return ICD;
    }
}
