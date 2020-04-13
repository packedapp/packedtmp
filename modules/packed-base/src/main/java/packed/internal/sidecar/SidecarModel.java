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
package packed.internal.sidecar;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.IdentityHashMap;
import java.util.Map;

import app.packed.base.Contract;
import app.packed.container.InternalExtensionException;
import app.packed.sidecar.Expose;
import app.packed.sidecar.PostSidecar;
import packed.internal.reflect.OpenClass;
import packed.internal.reflect.t2.FindConstructor;
import packed.internal.reflect.t2.InjectionSpec;
import packed.internal.util.UncheckedThrowableFactory;

/**
 * A model of a sidecar
 */
public abstract class SidecarModel {

    /** A method handle used for creating new sidecar instances. */
    protected final MethodHandle constructor;

    /** It is important this map is immutable as the key set is exposed via ExtensionDescriptor. */
    // Can 2 extensions define the same contract???? Don't think so
    // If not we could have a Contract.class->ContractFactory Map and a Contract.of(ContainerSource, Class<T extends
    // Contract>);
    protected final Map<Class<? extends Contract>, MethodHandle> contracts;

    /** The type of sidecar. */
    private final Class<?> sidecarType;

    final Object[] callbacks;

    /**
     * Creates a new sidecar model.
     * 
     * @param builder
     *            the builder
     */
    protected SidecarModel(Builder builder) {
        this.constructor = builder.constructor;
        this.sidecarType = builder.sidecarType;
        this.contracts = Map.copyOf(builder.contracts);
        this.callbacks = builder.callbacks;
    }

    public Map<Class<? extends Contract>, MethodHandle> contracts() {
        return contracts;
    }

    public void invokeCallbacks(int id, Object sidecar) {
        Object o = callbacks[id];
        if (o != null) {
            MethodHandle mh = (MethodHandle) o;
            try {
//                if (mh.type().parameterCount() == 0) {
//                    mh.invoke();
//                } else {
//                   
//                }

                mh.invoke(sidecar);
            } catch (Throwable e) {
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    public Class<?> sidecarType() {
        return sidecarType;
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

        final Object[] callbacks;

        protected Builder(SidecarTypeMeta statics, Class<?> sidecarType) {
            this.sidecarType = requireNonNull(sidecarType);
            this.statics = requireNonNull(statics);
            this.callbacks = new Object[statics.lifecycleStates.length];
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected OpenClass prep(InjectionSpec spec) {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), sidecarType, true);

            FindConstructor fc = new FindConstructor();
            this.constructor = fc.doIt(cp, spec);
            cp.findMethods(e -> {
                PostSidecar oa = e.getAnnotation(PostSidecar.class);
                if (oa != null) {
                    if (Modifier.isStatic(e.getModifiers())) {
                        throw new InternalExtensionException("Methods annotated with " + PostSidecar.class + " cannot be static, method = " + e);
                    }
                    int index = statics.indexOf(oa.value());
                    if (index == -1) {
                        throw new Error();
                    }

                    MethodHandle mh = cp.unreflect(e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);

                    callbacks[index] = mh;
                }
                Expose ex = e.getAnnotation(Expose.class);
                if (ex != null) {
                    if (e.getReturnType() == void.class) {
                        builderMethod = cp.unreflect(e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    } else {
                        MethodHandle mh = cp.unreflect(e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                        contracts.put((Class) e.getReturnType(), mh);
                    }
                }
            });

            return cp;
        }
    }
}
