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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.container.User;
import app.packed.operation.BindingMirror;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ClassUtil;

/**
 * A binding to a constant.
 */
// Tror maaske vi skal have en MH adaptor som kan wrappe den i Optional osv.
public final class ConstantBindingSetup extends BindingSetup {

    /** Non-null if bound by an application. */
    @Nullable
    private final ExtensionSetup boundBy;

    /** The constant that was bound. */
    @Nullable
    private final Object constant;

    /** Supplies a mirror for the operation */
    private final Supplier<? extends BindingMirror> specializedMirror;

    // Hmm, laver vi den her lazily, fx hvis vi kun vil mirrors
    final MethodHandle adaptor;

    public ConstantBindingSetup(OperationSetup operation, @Nullable ExtensionSetup boundBy, int index, Object constant, @Nullable MethodHandle adaptor,
            Supplier<? extends BindingMirror> specializedMirror) {
        super(operation, index);
        this.boundBy = boundBy;
        this.constant = constant;
        this.adaptor = adaptor;
        this.specializedMirror = specializedMirror;
    }

    /** {@inheritDoc} */
    @Override
    public User boundBy() {
        return boundBy == null ? User.application() : User.extension(boundBy.extensionType);
    }

    /** {@inheritDoc} */
    @Override
    protected BindingMirror mirror0() {
        return ClassUtil.mirrorHelper(BindingMirror.class, BindingMirror::new, specializedMirror);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle read() {
        MethodHandle mh = MethodHandles.constant(constant == null ? Object.class : constant.getClass(), constant);
        return MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
    }
}
