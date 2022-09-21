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
package internal.app.packed.service.build;

import java.lang.invoke.MethodHandle;

import app.packed.base.Key;
import app.packed.base.Nullable;
import internal.app.packed.operation.bindings.DependencyNode;
import internal.app.packed.service.runtime.DelegatingRuntimeService;
import internal.app.packed.service.runtime.RuntimeService;
import internal.app.packed.service.runtime.ServiceInstantiationContext;
import internal.app.packed.util.MethodHandleUtil;

/**
 * A build entry representing an existing service that has been given a new key.
 * 
 * @see ServiceComposer#rekey(Class, Class)
 * @see ServiceComposer#rekey(Key, Key)
 */
public final class RekeyServiceSetup extends ServiceSetup {

    /** The service that has been given a new key. */
    public final ServiceSetup serviceToRekey;

    /**
     * Create a new service.
     * 
     * @param service
     *            the service that is given a new key.
     * @param key
     *            the new key
     */
    public RekeyServiceSetup(ServiceSetup service, Key<?> key) {
        super(key);
        this.serviceToRekey = service;
    }

    @Override
    @Nullable
    public DependencyNode dependencyConsumer() {
        return serviceToRekey.dependencyConsumer();
    }

    @Override
    public MethodHandle dependencyAccessor() {
        MethodHandle mh = serviceToRekey.dependencyAccessor();
        return MethodHandleUtil.castReturnType(mh, key().rawType());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return serviceToRekey.isConstant();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        return new DelegatingRuntimeService(key(), serviceToRekey.toRuntimeEntry(context));
    }
}
