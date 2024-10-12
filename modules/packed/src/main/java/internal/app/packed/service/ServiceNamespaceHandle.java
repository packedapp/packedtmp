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
package internal.app.packed.service;

import java.util.Set;

import app.packed.binding.Key;
import app.packed.binding.KeyAlreadyProvidedException;
import app.packed.build.BuildActor;
import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceInstaller;
import app.packed.service.ServiceNamespaceConfiguration;
import app.packed.service.ServiceNamespaceMirror;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingAccessor;
import internal.app.packed.binding.BindingAccessor.FromLifetimeArena;
import internal.app.packed.binding.BindingAccessor.FromOperationResult;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderHandle;
import internal.app.packed.service.util.SequencedServiceMap;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.handlers.ServiceHandlers;

/**
 *
 */
public abstract class ServiceNamespaceHandle extends NamespaceHandle<BaseExtension, ServiceNamespaceConfiguration> {

    /** All service providers in the namespace. */
    final SequencedServiceMap<NamespaceServiceProviderHandle> providers = new SequencedServiceMap<>();

    /**
     * @param installer
     */
    protected ServiceNamespaceHandle(NamespaceInstaller<?> installer) {
        super(installer);
    }

    public Set<Key<?>> keys() {
        return providers.keySet();
    }

    /** {@inheritDoc} */
    @Override
    protected final ServiceNamespaceConfiguration newNamespaceConfiguration(BaseExtension e, BuildActor actor) {
        return ServiceHandlers.newServiceNamespaceConfiguration(this, e);
    }

    /** {@inheritDoc} */
    @Override
    protected final ServiceNamespaceMirror newNamespaceMirror() {
        return ServiceHandlers.newServiceNamespaceMirror(this);
    }

    /**
     * Provides a service for the specified operation.
     * <p>
     * This method is called either because a bean is registered directly via {@link BeanHandle#serviceProvideAs(Key)} or
     * from {@link BaseExtension#newBeanIntrospector} because someone used a {@link Provide} annotation.
     *
     * @param key
     *            the key to provide a service for
     * @param operation
     *            the operation that provides the service
     * @return a provided service
     */
    public NamespaceServiceProviderHandle provide(Key<?> key, OperationSetup operation, BindingAccessor resolution) {
        // Have no idea what we are doing here
        if (resolution instanceof FromLifetimeArena fla) {
            if (key.rawType() != fla.type()) {
                resolution = new FromLifetimeArena(fla.containerLifetime(), fla.index(), key.rawType());
            }
        }

        // Check if we have an existing service provider for the specified key
        NamespaceServiceProviderHandle existing = providers.get(key);
        if (existing != null) {
            throw new KeyAlreadyProvidedException(provideDublicateProvideErrorMsg(existing, operation));
        }

        // Create a new service provider and add it to the map of service providers
        // bean.install(::newHandle);
        NamespaceServiceProviderHandle newProvider = new NamespaceServiceProviderHandle(key, this, operation, resolution);
        providers.put(key, newProvider);
        return newProvider;
    }

    private String provideDublicateProvideErrorMsg(NamespaceServiceProviderHandle existingProvider, OperationSetup newOperation) {
        OperationSetup existingOperation = existingProvider.operation();

        Key<?> key = existingProvider.key();
        // The same bean providing the same service
        if (existingOperation.bean == newOperation.bean) {
            return "This bean is already providing a service for " + key.toString() + ", beanClass = "
                    + StringFormatter.format(existingOperation.bean.beanClass);
        }

        if (existingProvider.binding() instanceof FromLifetimeArena) {
            return "Cannot provide a service for " + key.toString() + ", as another bean of type " + StringFormatter.format(existingOperation.bean.beanClass)
                    + " is already providing a service for the same key";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";
        } else if (existingProvider.binding() instanceof FromOperationResult os) {
            if (os.operation().target instanceof MemberOperationTarget m && m.target instanceof OperationMethodTarget t) {
                String ss = StringFormatter.formatShortWithParameters(t.method());
                return "A method " + ss + " is already providing a service for " + key;
            }
        }
        return newOperation + " A service has already been bound for key " + key;
    }

    @Nullable
    public NamespaceServiceProviderHandle provider(Key<?> key) {
        return providers.get(key);
    }
}
