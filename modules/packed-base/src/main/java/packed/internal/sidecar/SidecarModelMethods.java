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
package packed.internal.sidecar;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import app.packed.inject.Provide;
import app.packed.statemachine.OnInitialize;

/**
 *
 */
final class SidecarModelMethods {

    private List<Method> provides = new ArrayList<>();

    private Method onInitialize;

    void test(Method m) {
        Provide ap = m.getAnnotation(Provide.class);
        if (ap != null) {
            provides.add(m);
        }

        OnInitialize oi = m.getAnnotation(OnInitialize.class);
        if (oi != null) {
            if (onInitialize != null) {
                throw new IllegalStateException(m.getDeclaringClass() + " defines more than one method with " + OnInitialize.class.getSimpleName());
            }
            onInitialize = m;
        }

    }
}
