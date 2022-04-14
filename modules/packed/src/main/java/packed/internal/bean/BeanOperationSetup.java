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
package packed.internal.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.bean.operation.mirror.OperationMirror;
import app.packed.extension.Extension;
import packed.internal.inject.DependencyNode;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Skal vi have flere forskellige???? fx Functional, Member ect... 
public final class BeanOperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(BeanOperationSetup)}. */
    private static final MethodHandle MH_INITIALIZE_OPERATIONS_SETUP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class,
            "initialize", void.class, BeanOperationSetup.class);

    /** The bean the operation belongs to. */
    public final BeanSetup bean;

    /** The operation's operator. */
    public final Class<? extends Extension<?>> operator;

    public DependencyNode depNode;

    public Supplier<? extends OperationMirror> mirrorSupplier;
    // dependencies

    public final boolean isRaw = false;
    
    public BeanOperationSetup(BeanSetup bean, Class<? extends Extension<?>> operator) {
        this.bean = bean;
        this.operator = operator;
    }

    OperationMirror mirror() {
        OperationMirror om;
        if (mirrorSupplier == null) {
            om = new OperationMirror();
        } else {
            om = mirrorSupplier.get();

        }
        try {
            MH_INITIALIZE_OPERATIONS_SETUP.invokeExact(om, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return om;
    }
}
