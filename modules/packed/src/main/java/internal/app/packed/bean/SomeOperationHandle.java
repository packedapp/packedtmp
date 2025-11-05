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
package internal.app.packed.bean;

import app.packed.operation.OperationHandle;

/**
 * An operation handle that can either be a primary bean or an applied sidebean.
 */
// Problemet er vi ikke kan extende denne, og fx gemme en MethodHandle
// Maaske tilfoejer vi bare operationen til primary bean.
// Nej det kan vi ikke fordi vi ikke nødvendigvis har en vi kan attache til.
// Fx primary har ikke start, men sidebean har
public sealed interface SomeOperationHandle<H extends OperationHandle<?>> {
    record OnPrimary<H extends OperationHandle<?>>(H handle) implements SomeOperationHandle<H> {}
    record OnSideBean<H extends OperationHandle<?>>(H handle, AppliedSideBean sidebean) implements SomeOperationHandle<H> {}
}
