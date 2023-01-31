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
package internal.app.packed.binding;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.bindings.BindingKind;
import app.packed.bindings.mirror.BindingMirror;
import app.packed.container.Realm;
import app.packed.framework.Nullable;
import internal.app.packed.binding.BindingSetup.HookBindingSetup;
import internal.app.packed.binding.BindingSetup.ManualBindingSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** The configuration of an operation's binding. */
public abstract sealed class BindingSetup permits ManualBindingSetup, HookBindingSetup, ServiceBindingSetup, ExtensionServiceBindingSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_BINDING_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), BindingMirror.class, "initialize",
            void.class, BindingSetup.class);

    /** The extension that owns the binding. */
    public final Realm boundBy;

    /** Supplies a mirror for the operation */
    public Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation this binding is a part of. */
    public final OperationSetup operation;

    /** The index into {@link OperationSetup#bindings}. */
    public final int operationBindingIndex;

    public BindingSetup(OperationSetup operation, int operationBindingIndex, Realm boundBy) {
        this.operation = requireNonNull(operation);
        this.operationBindingIndex = operationBindingIndex;
        this.boundBy = requireNonNull(boundBy);
    }

    public abstract BindingKind kind();

    /** {@return a new mirror.} */
    public BindingMirror mirror() {
        BindingMirror mirror = ClassUtil.mirrorHelper(BindingMirror.class, BindingMirror::new, mirrorSupplier);

        // Initialize BindingMirror by calling BindingMirror#initialize(BindingSetup)
        try {
            MH_BINDING_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public abstract BindingProvider provider();

    public static final class HookBindingSetup extends BindingSetup {

        /** Provider for the binding. */
        public final BindingProvider provider;

        public HookBindingSetup(OperationSetup operation, int index, Realm user, BindingProvider provider) {
            super(operation, index, user);
            this.provider = requireNonNull(provider);
        }

        /** {@inheritDoc} */
        @Override
        public BindingKind kind() {
            return BindingKind.HOOK;
        }

        @Override
        public BindingProvider provider() {
            return provider;
        }
    }

    public static final class ManualBindingSetup extends BindingSetup {

        /** Provider for the binding. */
        @Nullable
        public final BindingProvider provider;
        
        /**
         * @param operation
         * @param index
         * @param user
         */
        public ManualBindingSetup(OperationSetup operation, int index, Realm user, BindingProvider provider) {
            super(operation, index, user);
            this.provider = provider;
        }

        /** {@inheritDoc} */
        @Override
        public BindingKind kind() {
            return BindingKind.MANUAL;
        }

        /** {@inheritDoc} */
        @Override
        @Nullable
        public BindingProvider provider() {
            return provider;
        }
    }
}

//Taenkte i lang om denne skulle organiseres efter [Constant, Operation, Argument]... eller [Binding, Service, Manual]
//I sidste endte det paa at vi ikke ved med det samme om vi har en constant, operation, ... Fx fordi services
//bliver resolvet sent. Dette gjoerde at vi valgte den nuvaere strategi
