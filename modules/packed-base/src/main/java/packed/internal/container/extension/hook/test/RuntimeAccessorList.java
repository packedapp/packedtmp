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
package packed.internal.container.extension.hook.test;

import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.reflect.VarOperator;

/**
 *
 */
public class RuntimeAccessorList<T> {

    public <S> void readyAll(ComponentConfiguration cc, Class<S> sidecarType, BiConsumer<S, T> consumer) {}

    public RuntimeAccessorList<T> add(AnnotatedFieldHook<?> hook, VarOperator<T> operator) {
        return this;
    }
}
