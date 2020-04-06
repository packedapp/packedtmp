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
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Contract;
import app.packed.base.OnAssembling;
import app.packed.container.ExtensionSidecar;
import app.packed.container.InternalExtensionException;
import app.packed.hook.Expose;
import packed.internal.container.ExtensionSidecarModel;
import packed.internal.reflect.ConstructorFinder;
import packed.internal.reflect.OpenClass;
import packed.internal.util.UncheckedThrowableFactory;

/**
 * A model of a sidecar
 */
public abstract class SidecarModel {

    /** It is important this map is immutable as the key set is exposed via ExtensionDescriptor. */
    // Can 2 extensions define the same contract???? Don't think so
    // If not we could have a Contract.class->ContractFactory Map and a Contract.of(ContainerSource, Class<T extends
    // Contract>);
    protected final Map<Class<? extends Contract>, MethodHandle> contracts;

    /** A method handle used for creating new sidecar instances. */
    protected final MethodHandle constructor;

    private final Class<?> sidecarType;

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
        this.l = List.copyOf(builder.l);
    }

    final List<ECall> l;

    public Map<Class<? extends Contract>, MethodHandle> contracts() {
        return contracts;
    }

    public Class<?> sidecarType() {
        return sidecarType;
    }

    public static abstract class Builder {

        // Need to check that a contract never belongs to two extension.
        // Also, I think we want to do this atomically, so that we do not have half an extension registered somewhere.
        // This means we want to synchronize things.
        // So add all shit, quick validation-> Sync->Validate final -> AddAll ->UnSync
        protected final IdentityHashMap<Class<? extends Contract>, MethodHandle> contracts = new IdentityHashMap<>();

        protected final Class<?> sidecarType;

        final SidecarTypeMeta statics;

        public final ArrayList<ECall> l = new ArrayList<>();

        public MethodHandle builderMethod;

        /** The constructor used to create a new extension instance. */
        private MethodHandle constructor;

        protected Builder(SidecarTypeMeta statics, Class<?> sidecarType) {
            this.sidecarType = requireNonNull(sidecarType);
            this.statics = requireNonNull(statics);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected OpenClass prep() {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), sidecarType, true);
            this.constructor = ConstructorFinder.find(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);

            cp.findMethods(e -> {
                OnAssembling oa = e.getAnnotation(OnAssembling.class);
                if (oa != null) {
                    if (Modifier.isStatic(e.getModifiers())) {
                        throw new InternalExtensionException("Methods annotated with " + OnAssembling.class + " cannot be static, method = " + e);
                    }
                    MethodHandle mh = cp.unreflect(e, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    l.add(new ECall(mh, oa.value().equals(ExtensionSidecar.ON_INSTANTIATION), oa.value().equals(ExtensionSidecar.ON_PREEMBLE)));
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

    public void invokeCallbacks(int id, Object sidecar) {
        if (id == ExtensionSidecarModel.ON_INSTANTIATION) {
            for (var v : l) {
                if (v.onInstantiation) {
                    try {
                        v.mh.invoke(sidecar);
                    } catch (Throwable e) {
                        throw new UndeclaredThrowableException(e);
                    }
                }
            }
        } else if (id == ExtensionSidecarModel.ON_PREEMBLE) {
            for (var v : l) {
                if (v.onMainFinished) {
                    try {
                        v.mh.invoke(sidecar);
                    } catch (Throwable e) {
                        throw new UndeclaredThrowableException(e);
                    }
                }
            }
        }
    }

    public static class ECall {
        public final MethodHandle mh;
        public final boolean onInstantiation;
        public final boolean onMainFinished;

        public ECall(MethodHandle mh, boolean onInstantiation, boolean onMainFinished) {
            this.mh = requireNonNull(mh);
            this.onInstantiation = onInstantiation;
            this.onMainFinished = onMainFinished;
        }
    }
}
