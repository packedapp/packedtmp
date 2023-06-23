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
package sandbox.extension.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import app.packed.extension.Extension;
import app.packed.operation.OperationMirror;

/**
 *
 */
// <T> <- which can be sorted???

// Nesting??? Fx for Container Lifecycle

// B1
// B1->create
// B1->Inject1
// B1->Inject2

// B2

public interface CompositeOperationHandle {

    void addChild(CompositeOperationHandle handle);

    void addChild(OperationHandle handle);

    MethodHandle generateMethodHandle();

    /**
     * {@return the invocation type of this operation.}
     * <p>
     * Method handles generated via {@link #generateMethodHandle()} will always return their {@link MethodHandle#type()
     * type} as the returned value
     *
     * @see OperationTemplate
     */
    MethodType invocationType();

    // Ogsaa en template ting taenker jeg? IDK
    void named(String name);

    /** {@return the operator of the operation.} */
    Class<? extends Extension<?>> operator();

    /**
     * Specializes the mirror that is returned for the operation.
     * <p>
     * The specified supplier may be called multiple times for the same operation.
     * <p>
     * The specified supplier should never return {@code null}.
     *
     * @param supplier
     *            a mirror supplier that is called if a mirror is required
     * @throws IllegalStateException
     *             if the operation is no longer configurable
     */
    void specializeMirror(Supplier<? extends OperationMirror> supplier);
}

// 1 Shared
interface Zalternative {
    boolean isComposite();

    // Mirrors are not enforced
}