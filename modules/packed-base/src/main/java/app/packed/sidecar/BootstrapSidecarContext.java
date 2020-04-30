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
package app.packed.sidecar;

import java.util.Map;
import java.util.function.Supplier;

import app.packed.container.Bundle;
import app.packed.container.Wirelet;

/**
 *
 */
interface BootstrapSidecarContext {
    void singleton(Class<?> cl);

    void link(Supplier<Map.Entry<Bundle, Wirelet[]>> e);// ugly.. Need a delaylink
}

// 3 levels of runtime sidecars

// via @SomeSidecar(runtimeSingletons = X.class)

// via BootstrapSidecarContext.install(Foo.class) <- per annotation... For extensions it would be constants

// at runtime via SidecarContext