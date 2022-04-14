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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.bean.operation.mirror.OperationMirror;
import app.packed.extension.Extension;
import packed.internal.bean.BeanSetup;
import packed.internal.inject.DependencyNode;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Skal vi have flere forskellige???? fx Functional, Member ect... 
public final class OperationSetup {

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
    public final Class<? extends Extension<?>> operator;

    public OperationSetup(BeanSetup bean, Class<? extends Extension<?>> operator) {
        this.bean = bean;
        this.operator = operator;
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
