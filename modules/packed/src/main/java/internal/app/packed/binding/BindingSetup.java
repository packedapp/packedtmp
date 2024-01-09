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

import java.util.function.Supplier;

import app.packed.component.ComponentOperator;
import app.packed.operation.BindingKind;
import app.packed.operation.BindingMirror;
import app.packed.util.Nullable;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.types.ClassUtil;

/** The configuration of an operation's binding. */
public abstract sealed class BindingSetup permits BindingSetup.ManualBindingSetup, BindingSetup.HookBindingSetup, ServiceBindingSetup {

    /** A magic initializer for {@link BindingMirror}. */
    public static final MagicInitializer<BindingSetup> MIRROR_INITIALIZER = MagicInitializer.of(BindingMirror.class);

    /**
     * The realm that owns the binding.
     * <p>
     * May be the application itself if using {@link app.packed.operation.Op#bind(Object)} or similar.
     */
    public final ComponentOperator boundBy;

    /** Supplies a mirror for the binding */
    public Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation this binding is a part of. */
    public final OperationSetup operation;

    /** The index into {@link OperationSetup#bindings}. */
    public final int operationBindingIndex;

    protected BindingSetup(OperationSetup operation, int operationBindingIndex, ComponentOperator boundBy) {
        this.operation = requireNonNull(operation);
        this.operationBindingIndex = operationBindingIndex;
        this.boundBy = requireNonNull(boundBy);
    }

    /** {@return the binding kind.} */
    public abstract BindingKind kind();

    /** {@return a new mirror.} */
    public BindingMirror mirror() {
        return MIRROR_INITIALIZER.run(() -> ClassUtil.newMirror(BindingMirror.class, BindingMirror::new, mirrorSupplier), this);
    }

    public abstract BindingResolution resolver();

    /**
     * A binding that was created do to some kind of binding hook.
     *
     * @see app.packed.bean.BeanHook.AnnotatedBindingHook
     * @see app.packed.bean.BeanHook.BindingTypeHook
     */
    public static final class HookBindingSetup extends BindingSetup {

        /** Provider for the binding. */
        public final BindingResolution provider;

        public HookBindingSetup(OperationSetup operation, int index, ComponentOperator user, BindingResolution provider) {
            super(operation, index, user);
            this.provider = requireNonNull(provider);
        }

        /** {@inheritDoc} */
        @Override
        public BindingKind kind() {
            return BindingKind.HOOK;
        }

        @Override
        public BindingResolution resolver() {
            return provider;
        }
    }

    /**
     * A binding that was created manually.
     *
     * @see app.packed.operation.OperationHandle#manuallyBindable(int)
     * @see app.packed.operation.Op#bind(Object)
     * @see app.packed.operation.Op#bind(int, Object, Object...)
     */
    public static final class ManualBindingSetup extends BindingSetup {

        /** Provider for the binding. */
        @Nullable
        public final BindingResolution provider;

        /**
         * @param operation
         * @param index
         * @param user
         */
        public ManualBindingSetup(OperationSetup operation, int index, ComponentOperator user, BindingResolution provider) {
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
        public BindingResolution resolver() {
            return provider;
        }
    }
}

//Taenkte i lang om denne skulle organiseres efter [Constant, Operation, Argument]... eller [Binding, Service, Manual]
//I sidste endte det paa at vi ikke ved med det samme om vi har en constant, operation, ... Fx fordi services
//bliver resolvet sent. Dette gjoerde at vi valgte den nuvaere strategi
