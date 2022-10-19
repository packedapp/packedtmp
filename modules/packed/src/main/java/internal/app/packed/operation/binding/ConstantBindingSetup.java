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

import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.container.UserOrExtension;
import app.packed.operation.BindingMirror;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ClassUtil;

/**
 * A binding to a constant.
 */
public final class ConstantBindingSetup extends BindingSetup {

    /** The constant that was bound. */
    @Nullable
    public final Object constant;

    // Eller er det en extension bean??? Det er hvem der styrer vaerdien
    @Nullable
    public ExtensionSetup boundBy;

    /** Supplies a mirror for the operation */
    private final Supplier<? extends BindingMirror> specializedMirror;

    public ConstantBindingSetup(OperationSetup operation, ExtensionSetup boundBy, int index, Object constant,
            Supplier<? extends BindingMirror> specializedMirror) {
        super(operation, index);
        this.boundBy = boundBy;
        this.constant = constant;
        this.specializedMirror = specializedMirror;
    }

    /** {@inheritDoc} */
    @Override
    protected BindingMirror mirror0() {
        return ClassUtil.mirrorHelper(BindingMirror.class, BindingMirror::new, specializedMirror);
    }

    /** {@inheritDoc} */
    @Override
    public UserOrExtension boundBy() {
        return boundBy == null ? UserOrExtension.application() : UserOrExtension.extension(boundBy.extensionType);
    }
}
