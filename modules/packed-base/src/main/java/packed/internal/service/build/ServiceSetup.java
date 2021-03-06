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
package packed.internal.service.build;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.attribute.AttributeMap;
import app.packed.base.Key;
import app.packed.service.Provide;
import app.packed.service.Service;
import packed.internal.inject.dependency.DependencyProducer;
import packed.internal.service.InternalService;
import packed.internal.service.runtime.RuntimeService;
import packed.internal.service.runtime.ServiceInstantiationContext;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * Instances of this class are only exposed as a {@link Service} to end users if {@link #isKeyFrozen}. 
 */
public abstract class ServiceSetup implements InternalService, DependencyProducer {

    private boolean isKeyFrozen;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    private Key<?> key;

    ServiceSetup(Key<?> key) {
        this.key = requireNonNull(key);
    }

    public final void as(Key<?> key) {
        if (isKeyFrozen) {
            throw new IllegalStateException("The key of the service can no longer be changed");
        }
        // requireConfigurable();
        // validateKey(key);
        // Det er sgu ikke lige til at validere det med generics signature....
        // Vi validere kun raw type
        this.key = requireNonNull(key, "key is null");
    }

    @Override
    public final <T> ServiceSetup decorate(Function<? super T, ? extends T> decoratingFunction) {
        return new MappingServiceSetup(this, key, decoratingFunction);
    }

    public final Service exposeAsService() {
        if (isKeyFrozen) {
            return this;
        }
        return simple(key, isConstant());
    }

    public final void freeze() {
        isKeyFrozen = true;
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
        public AttributeMap attributes() {
            return AttributeMap.of();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Service[key=" + key + "]";
        }
    }
}
