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
package packed.internal.bean.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.bean.operation.mirror.OperationMirror;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.inject.DependencyNode;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class OperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_OPERATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class,
            "initialize", void.class, OperationSetup.class);

    /** The bean the operation is a part of. */
    public final BeanSetup bean;

    public DependencyNode depNode;

    public final boolean isRaw = false;

    public Supplier<? extends OperationMirror> mirrorSupplier;
    // dependencies

    /** The operation's operator. */
    public final ExtensionSetup extension;

    /** The operation's target. */
    public final PackedOperationTarget target;

    public OperationSetup(BeanSetup bean, PackedOperationTarget target, ExtensionSetup extension) {
        this.bean = requireNonNull(bean);
        this.target = requireNonNull(target);
        this.extension = requireNonNull(extension);
        bean.addOperation(this);
    }

    /** {@return a mirror for the operation.} */
    public OperationMirror mirror() {
        OperationMirror om;
        if (mirrorSupplier == null) {
            om = new OperationMirror();
        } else {
            om = mirrorSupplier.get();

        }
        try {
            MH_OPERATION_MIRROR_INITIALIZE.invokeExact(om, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return om;
    }
}
