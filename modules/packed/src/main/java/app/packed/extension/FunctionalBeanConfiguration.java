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
package app.packed.extension;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.extension.bean.BeanHandle;
import app.packed.extension.operation.DelegatingOperationHandle;
import app.packed.extension.operation.OperationHandle;
import app.packed.util.FunctionType;
import app.packed.util.Key;

/**
 * Represents a (source-less) bean that can have multiple functions attached.
 * <p>
 * {@link BaseExtensionPoint#installFunctional()}
 *
 * @see BaseExtensionPoint#installFunctional()
 */
public class FunctionalBeanConfiguration extends BeanConfiguration {

    /**
     * @param handle
     */
    FunctionalBeanConfiguration(BeanHandle<?> handle) {
        super(handle);
    }

    // Maaske vi har en FunctionTemplate der pakker interface + operation type + prefix

    public OperationHandle addFunction(Class<?> functionalInterface, Object function, FunctionType operationType) {
        throw new UnsupportedOperationException();
    }

    // We need a extension bean
    // Dem der resolver bindings, skal goeres mens man introspector...
    // Burde have en OperationType uden annoteringer
    // Maaske bare stripper annoteringer...
    // Men okay vi kan stadig fx bruge Logger som jo stadig skulle
    // supplies uden et hook
    public OperationHandle addFunction(InstanceBeanConfiguration<?> operator, Class<?> functionalInterface, FunctionType type, Object functionInstance) {
        // I think we can ignore the operator now.

        // Function, OpType.of(void.class, HttpRequest.class, HttpResponse.class), someFunc)
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public FunctionalBeanConfiguration named(String name) {
        super.named(name);
        return this;
    }

    public DelegatingOperationHandle newDelegationFunctionalOperation(Class<?> functionalInterface, Object function, FunctionType operationType) {
        // We only take public exported types
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <K> FunctionalBeanConfiguration overrideService(Class<K> key, K instance) {
        super.overrideService(key, instance); // will always fail, because there are no services
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> FunctionalBeanConfiguration overrideService(Key<K> key, K instance) {
        super.overrideService(key, instance); // will always fail, because there are no services
        return this;
    }
}

//
// Could have a StaticBeanConfiguration as this is the only reason
// override service is on BeanConfiguration

// and then not have overrideService on BeanConfiguration?

// Nah.. Saa skal vi vel have en common super class for baade
// StaticBeanConfiguration og InstanceBeanConfiguration