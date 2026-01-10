/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package sandbox.operation.mirror;

/**
 *
 */
// What about @Default... Optional, osv)
// Er ikke super interessant for brugere...
public enum BindingProviderKind {

    /** A constant. */
    CONSTANT,

    /** An argument that is provided when invoking the operation. */
    ARGUMENT,

    /** The binding is a result of another operation (possible an arguments to the operation). */
    OPERATION_RESULT;
}
// LifetimeConstant