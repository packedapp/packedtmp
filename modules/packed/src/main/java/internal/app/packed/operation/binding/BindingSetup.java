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

import app.packed.container.ApplicationOrExtension;
import app.packed.operation.BindingMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * The internal configuration of a single binding.
 */
public abstract class BindingSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_BINDING_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BindingMirror.class, "initialize",
            void.class, BindingSetup.class);

    /** The index into {@link OperationSetup#bindings}. */
    public final int index;

    /** The underlying operation. */
    public final OperationSetup operation;

    public BindingSetup(OperationSetup operation, int index) {
        this.operation = operation;
        this.index = index;
    }

    public abstract ApplicationOrExtension boundBy();
    
    /** {@return a new mirror.} */
    public BindingMirror mirror() {
        BindingMirror mirror = mirror0();
        
        // Initialize BindingMirror by calling BindingMirror#initialize(BindingSetup)
        try {
            MH_BINDING_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }
    
    protected abstract BindingMirror mirror0();
}
