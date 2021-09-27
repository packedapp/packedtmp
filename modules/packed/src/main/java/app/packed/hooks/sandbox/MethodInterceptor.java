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
package app.packed.hooks.sandbox;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 *
 */
// En gammel klasse Tror vi bruger et hook...
interface MethodInterceptor {

    default MethodType sourceType() {
        throw new UnsupportedOperationException();
    }

    void source(MethodHandle mh);

    /**
     * The method handle to invoke or intercept.
     * 
     * @return the method handle to invoke or intercept
     */
    MethodHandle target();

    default MethodType targetType() {
        return target().type();
    }
}
// source changes as we
