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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

import packed.internal.classscan.invoke.MethodHandleBuilder;
import packed.internal.classscan.invoke.OpenClass;
import packed.internal.sidecar.model.Model;
import packed.internal.sidecar.old.SidecarTypeMeta;

/**
 * A model of a sidecar.
 */
public abstract class AbstractExtensionModel extends Model {

    /** A method handle for creating a new sidecar instance. */
    protected final MethodHandle constructor;

    /** It is important this map is immutable as the key set is exposed via ExtensionDescriptor. */
    // Can 2 extensions define the same contract???? Don't think so
    // If not we could have a Contract.class->ContractFactory Map and a Contract.of(ContainerSource, Class<T extends
    // Contract>);
    // Contract must use MemberOfExtension
    // And not provide other constract
    protected final Map<Class<? extends Contract>, MethodHandle> contracts;

    /**
     * Creates a new sidecar model.
     * 
     * @param builder
     *            the builder
     */
    protected AbstractExtensionModel(Builder builder) {
        super(builder.sidecarType);
        this.constructor = builder.constructor;
        this.contracts = Map.copyOf(builder.contracts);
    }

    public Map<Class<? extends Contract>, MethodHandle> contracts() {
        return contracts;
    }

    public static abstract class Builder {

        public MethodHandle builderMethod;

        /** The constructor used to create a new extension instance. */
        private MethodHandle constructor;

        // Need to check that a contract never belongs to two extension.
        // Also, I think we want to do this atomically, so that we do not have half an extension registered somewhere.
        // This means we want to synchronize things.
        // So add all shit, quick validation-> Sync->Validate final -> AddAll ->UnSync
        protected final IdentityHashMap<Class<? extends Contract>, MethodHandle> contracts = new IdentityHashMap<>();

        protected final Class<?> sidecarType;

        final SidecarTypeMeta statics;

        protected Builder(Class<?> sidecarType, SidecarTypeMeta statics) {
            this.sidecarType = requireNonNull(sidecarType);
            this.statics = requireNonNull(statics);
        }

        protected void decorateOnSidecar(MethodHandleBuilder builder) {}

        protected void onMethod(Method m) {}

        protected OpenClass prep(MethodHandleBuilder spec) {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), sidecarType, true);
            this.constructor = cp.findConstructor(spec);
            return cp;
        }
    }

}
