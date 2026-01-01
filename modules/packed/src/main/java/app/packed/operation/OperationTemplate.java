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
package app.packed.operation;

import app.packed.context.Context;
import internal.app.packed.operation.PackedOperationTemplate;

/**
 * An operation template defines the basic behaviour of an operation.
 * <p>
 *
 * and is typically reused across multiple operations.
 *
 *
 * <p>
 */

// Return types
/// Checks
/// Mappers (fx sealed record faetter)... Du kan returner Foo, Void, Eller bla
/// Sidecar extractor

@Deprecated
public sealed interface OperationTemplate permits PackedOperationTemplate {

    // Tror ikke laengere man kan lave dem direkte paa den her maade...
    @Deprecated
    static OperationTemplate defaults() {
        return PackedOperationTemplate.DEFAULTS;
    }

    // Hvad sker der naar den er i andre lifetimes?
    @Deprecated
    OperationTemplate withContext(Class<? extends Context<?>> context);

    @Deprecated
    OperationTemplate withRaw();

    @Deprecated
    /** {@return an operation template that ignores any return value.} */
    // If you want to fail. Check return type
    // Isn't it just void???
    // I think this won't fail unlike returnType(void.class)
    // which requires void return type
    OperationTemplate withReturnIgnore();

    @Deprecated
    OperationTemplate withReturnType(Class<?> type);

    // Field type, Method return Type
    // The operation template will be re-adjusted before being used

    @Deprecated
    OperationTemplate withReturnTypeDynamic();
}

