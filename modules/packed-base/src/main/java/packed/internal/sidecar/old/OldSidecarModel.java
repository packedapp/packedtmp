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
package packed.internal.sidecar.old;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import app.packed.base.Contract;
import app.packed.container.InternalExtensionException;
import app.packed.statemachine.Leaving;
import app.packed.statemachine.StateTransition;
import packed.internal.invoke.MethodHandleBuilder;
import packed.internal.invoke.OpenClass;
import packed.internal.lifecycle.old.DefaultLifecycleTransition;
import packed.internal.sidecar.model.Model;
import packed.internal.util.ThrowableUtil;

/**
 * A model of a sidecar.
 */
public abstract class OldSidecarModel extends Model {

    /** A method handle for creating a new sidecar instance. */
    protected final MethodHandle constructor;

    /** It is important this map is immutable as the key set is exposed via ExtensionDescriptor. */
    // Can 2 extensions define the same contract???? Don't think so
    // If not we could have a Contract.class->ContractFactory Map and a Contract.of(ContainerSource, Class<T extends
    // Contract>);
    // Contract must use MemberOfExtension
    // And not provide other constract
    protected final Map<Class<? extends Contract>, MethodHandle> contracts;

    /** Methods annotated with {@link Leaving}. Takes the sidecar instance */
    // Det kan jo vaere gemt i en int istedet for saa vi bare test noget modulo...
    private final MethodHandle[] postSidecars; // TODO take a SidecarModel as well as well???

    /**
     * Creates a new sidecar model.
     * 
     * @param builder
     *            the builder
     */
    protected OldSidecarModel(Builder builder) {
        super(builder.sidecarType);
        this.constructor = builder.constructor;
        this.contracts = Map.copyOf(builder.contracts);
        this.postSidecars = builder.postSidecars;
    }

    public Map<Class<? extends Contract>, MethodHandle> contracts() {
        return contracts;
    }

    public void invokePostSidecarAnnotatedMethods(int stateId, Object sidecar, Object context) {
        MethodHandle mh = postSidecars[stateId];
        if (mh != null) {
            try {
                mh.invokeExact(sidecar, context);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
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

        private final MethodHandle[] postSidecars;

        protected final Class<?> sidecarType;

        final SidecarTypeMeta statics;

        protected Builder(Class<?> sidecarType, SidecarTypeMeta statics) {
            this.sidecarType = requireNonNull(sidecarType);
            this.statics = requireNonNull(statics);
            this.postSidecars = new MethodHandle[statics.ld.numberOfStates()];
        }

        protected void decorateOnSidecar(MethodHandleBuilder builder) {}

        protected void onMethod(Method m) {}

        protected OpenClass prep(MethodHandleBuilder spec) {
            OpenClass cp = new OpenClass(MethodHandles.lookup(), sidecarType, true);

            this.constructor = cp.findConstructor(spec);
            cp.findMethods(m -> {
                onMethod(m);
                Leaving oa = m.getAnnotation(Leaving.class);
                if (oa != null) {
                    // To support nextStates. We create a MH filter with a guard on the next state...
                    // To support Error transitions. I think we have specific code for this
                    // It is not performance criticial... So we do not want to check this normally...

                    // Validate states
                    String state = oa.state();
                    int index = statics.ld.indexOf(state);
                    if (index == -1) {
                        // TODO remove instantiating, available values
                        throw new InternalExtensionException("Unknown sidecar lifecycle event '" + state + "' for method " + m + ", available states are "
                                + Arrays.toString(statics.ld.toArray()));
                    }

                    MethodHandleBuilder mhb = MethodHandleBuilder.of(void.class, Object.class, Object.class);
                    decorateOnSidecar(mhb);
                    MethodHandle lt = MethodHandles.constant(StateTransition.class, new DefaultLifecycleTransition("Gll", "F", "FordiDuErDum"));
                    lt = MethodHandles.dropArguments(lt, 0, Object.class);
                    mhb.addKey(StateTransition.class, lt, 0);
                    MethodHandle mh = mhb.build(cp, m);

                    // If there are multiple methods with the same index. We just fold them to a single MethodHandle
                    MethodHandle existing = postSidecars[index];
                    postSidecars[index] = existing == null ? mh : MethodHandles.foldArguments(existing, mh);
                }
//                Expose ex = m.getAnnotation(Expose.class);
//                if (ex != null) {
//                    if (m.getReturnType() == void.class) {
//                        builderMethod = cp.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
//                    } else {
//                        MethodHandle mh = cp.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
//                        contracts.put((Class) m.getReturnType(), mh);
//                    }
//                }
            });
            return cp;
        }
    }

}