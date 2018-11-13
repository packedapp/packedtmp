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

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Supplier;

import app.packed.inject.BindingMode;
import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.InjectionSite;
import app.packed.inject.Key;
import app.packed.inject.Provider;
import app.packed.inject.ServiceDescriptor;
import packed.internal.inject.Node;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.inject.runtimenodes.RuntimeNodeAlias;
import packed.internal.inject.runtimenodes.RuntimeNodeFactory;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A build that imports a service from another injector.
 */
public class BuildNodeImportServiceFromInjector<T> extends BuildNode<T> implements Provider<T> {

    /** The descriptor of the service we are importing from. */
    final ServiceDescriptor descriptor;

    final Key<T> injectorKey;

    final RuntimeNode<T> node;

    /** The injector */
    final ImportServicesFromInjector stagingArea;

    /**
     * @param injectorConfiguration
     * @param dependencies
     * @param stackframe
     */
    @SuppressWarnings("unchecked")
    public BuildNodeImportServiceFromInjector(ImportServicesFromInjector stagingArea, InternalConfigurationSite configurationSite,
            ServiceDescriptor descriptor) {
        super(stagingArea.injectorConfiguration, configurationSite, List.of());
        this.stagingArea = requireNonNull(stagingArea);
        this.descriptor = requireNonNull(descriptor);
        this.node = descriptor instanceof Node ? (RuntimeNode<T>) descriptor : null;
        this.injectorKey = (Key<T>) requireNonNull(descriptor.getKey());
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return getInstance(null);
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return descriptor.getBindingMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        if (node == null) {
            return stagingArea.injector.get(injectorKey).get();
        }
        return node.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return node.needsInjectionSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return false;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    RuntimeNode<T> newRuntimeNode() {
        if (node == null) {
            Factory<T> f = Factory0.of((Supplier) () -> stagingArea.injector.get(injectorKey).get(), injectorKey.getTypeLiteral());
            return new RuntimeNodeFactory<>(this, InternalFactory.from(f));
        }
        return new RuntimeNodeAlias<T>(this, node);
    }
}
