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
package app.packed.bean.lifecycle;

import java.util.Optional;
import java.util.stream.Stream;

import app.packed.build.Mirror;

/**
 * This mirror represents the lifecycle of a bean.
 *
 * @see app.packed.bean.BeanMirror#lifecycle()
 */
public interface BeanLifecycleMirror extends Mirror {

    Optional<InitializeOperationMirror> factory();

    /** {@return a list of all initialization operations on the bean, in the order they will be invoked} */
    Stream<InitializeOperationMirror> initializers();

    /** {@return the beans lifecycle kind} */
    BeanLifecycleModel kind();

    /** {@return a list of all start operations on the bean, in the order they will be invoked} */
    Stream<StartOperationMirror> starters();

    /** {@return a list of all stop operations on the bean, in the order they will be invoked} */
    Stream<StopOperationMirror> stoppers();
}
