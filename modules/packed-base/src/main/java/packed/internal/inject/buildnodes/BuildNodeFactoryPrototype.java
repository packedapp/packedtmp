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
package packed.internal.inject.buildnodes;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.inject.runtimenodes.RuntimeNodeFactory;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 *
 */
public class BuildNodeFactoryPrototype<T> extends BuildNodeFactory<T> {

    public BuildNodeFactoryPrototype(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, InternalFactory<T> factory) {
        super(injectorConfiguration, configurationSite, factory);
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return BindingMode.PROTOTYPE;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return hasDependencyOnInjectionSite;
    }

    /** {@inheritDoc} */
    @Override
    RuntimeNode<T> newRuntimeNode() {
        return new RuntimeNodeFactory<>(this, factory);
    }
}
