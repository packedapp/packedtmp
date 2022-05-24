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
package packed.internal.operation;

import java.lang.invoke.MethodHandle;

import app.packed.operation.OperationSiteMirror;
import packed.internal.bean.hooks.PackedBeanMember;

/** The target of an operation. */
public sealed interface PackedOperationTarget permits PackedBeanMember, PackedFunctionOperationTarget {

    default MethodHandle methodHandle() {
        throw new UnsupportedOperationException();
    }

    /** {@return a mirror representing the target.} */
    public abstract OperationSiteMirror mirror();
}
