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
package internal.app.packed.operation.binding;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.container.Realm;
import app.packed.framework.Nullable;
import app.packed.operation.BindingMirror;
import app.packed.operation.OperationMirror;
import app.packed.operation.bindings.BindingKind;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The configuration of a single binding in an operation. */
public abstract class BindingSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_BINDING_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BindingMirror.class, "initialize",
            void.class, BindingSetup.class);

    public final Realm boundBy;

    /** The index into {@link OperationSetup#bindings}. */
    public final int index;

    /** Supplies a mirror for the operation */
    public Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation this binding is a part of. */
    public final OperationSetup operation;

    /** Provider for the binding. */
    @Nullable
    public BindingProvider provider;

    public BindingSetup(OperationSetup operation, int index, Realm user) {
        this.operation = requireNonNull(operation);
        this.index = index;
        this.boundBy = requireNonNull(user);
    }

    public abstract MethodHandle bindIntoOperation(MethodHandle methodHandle);

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

    public static final class HookBindingSetup extends BindingSetup {

        /**
         * @param operation
         * @param index
         * @param user
         */
        public HookBindingSetup(OperationSetup operation, int index, Realm user) {
            super(operation, index, user);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public BindingKind kind() {
            return BindingKind.BINDING_ANNOTATION;
        }
    }

    public static final class ManualBindingSetup extends BindingSetup {

        /**
         * @param operation
         * @param index
         * @param user
         */
        public ManualBindingSetup(OperationSetup operation, int index, Realm user) {
            super(operation, index, user);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public BindingKind kind() {
            return BindingKind.MANUAL;
        }
    }
}

//Taenkte i lang om denne skulle organiseres efter [Constant, Operation, Argument]... eller [Binding, Service, Manual]
//I sidste endte det paa at vi ikke ved med det samme om vi har en constant, operation, ... Fx fordi services
//bliver resolvet sent. Dette gjoerde at vi valgte den nuvaere strategi
