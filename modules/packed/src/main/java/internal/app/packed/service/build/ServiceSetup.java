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

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.base.Key;
import app.packed.service.Provide;
import internal.app.packed.operation.oldbindings.DependencyProducer;
import internal.app.packed.service.InternalService;
import internal.app.packed.service.runtime.RuntimeService;
import internal.app.packed.service.runtime.ServiceInstantiationContext;
import internal.app.packed.service.sandbox.Service;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * Instances of this class are only exposed as a {@link Service} to end users if {@link #isKeyFrozen}. 
 */
public abstract non-sealed class ServiceSetup implements InternalService, DependencyProducer {

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    private final Key<?> key;

    ServiceSetup(Key<?> key) {
        this.key = requireNonNull(key);
    }

    @Override
    public final <T> ServiceSetup decorate(Function<? super T, ? extends T> decoratingFunction) {
        return new MappingServiceSetup(this, key, decoratingFunction);
    }

    @Override
    public abstract boolean isConstant();

    @Override
    public final Key<?> key() {
        return key;
    }

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract RuntimeService newRuntimeNode(ServiceInstantiationContext context);

    @Override
    public final ServiceSetup rekeyAs(Key<?> key) {
        // NewKey must be compatible with type
        RekeyServiceSetup esb = new RekeyServiceSetup(this, key);
        return esb;
    }

    // cacher runtime noden...
    public final RuntimeService toRuntimeEntry(ServiceInstantiationContext context) {
        return context.transformers.computeIfAbsent(this, k -> {
            return k.newRuntimeNode(context);
        });
    }

    // Maaske flyt den til service... Naar vi sealer ting.. Er det godt at give folk en 
    // mulighed for at kunne instanser af dem
    public static Service simple(Key<?> key, boolean isConstant) {
        return new ServiceWrapper(key, isConstant);
    }
    
    /** An implementation of {@link Service} because {@link ServiceSetup} is mutable. */
    private static final record ServiceWrapper(Key<?> key, boolean isConstant) implements Service {

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Service[key=" + key + "]";
        }
    }
}
