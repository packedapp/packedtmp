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
import java.util.Set;

import app.packed.bundle.Bundle;
import app.packed.bundle.WiringOperation;
import app.packed.inject.Factory;
import app.packed.inject.Injector;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.SimpleInjectorConfigurator;
import app.packed.util.Nullable;
import packed.internal.inject.builder.InjectorBuilder;

/**
 *
 */
public class SimpleInjectorConfiguratorImpl implements SimpleInjectorConfigurator {

    InjectorBuilder builder;

    public SimpleInjectorConfiguratorImpl(InjectorBuilder ib) {
        this.builder = requireNonNull(ib);
    }

    @Override
    public @Nullable String getDescription() {
        return builder.getDescription();
    }

    @Override
    public void lookup(Lookup lookup) {
        builder.lookup(lookup);
    }

    @Override
    public <T> ServiceConfiguration<T> provide(Factory<T> factory) {
        return builder.provide(factory);
    }

    @Override
    public <T> ServiceConfiguration<T> provide(T instance) {
        return builder.provide(instance);
    }

    @Override
    public void registerStatics(Class<?> staticsHolder) {
        builder.registerStatics(staticsHolder);
    }

    @Override
    public SimpleInjectorConfigurator setDescription(@Nullable String description) {
        builder.setDescription(description);
        return this;
    }

    @Override
    public Set<String> tags() {
        return builder.tags();
    }

    @Override
    public void wireInjector(Bundle bundle, WiringOperation... stages) {
        builder.wireInjector(bundle, stages);
    }

    @Override
    public void wireInjector(Injector injector, WiringOperation... stages) {
        builder.wireInjector(injector, stages);
    }
}
