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

import java.util.Set;

import app.packed.component.ComponentPath;
import app.packed.context.Context;

/**
 *
 */
// Implements Serializable??

// Important that we only make one of them per operation.
// Because if we use it as a key in a map. The equals method is going to be very long
// Maybe we compare for Identity only...

// Is Static!
// Maybe we can use already with interceptors filters
public interface OperationSite {
    Class<?> beanClass();

    ComponentPath beanComponentPath();

    String beanName();

    ComponentPath containerComponentPath();

    String containerName();

    String containerPath();

    Set<Class<? extends Context<?>>> contexts();

    /** {@return the component path of the operation} */
    ComponentPath operationComponentPath();

    /** {@return the name of the operation} */
    String operationName();

    /** {@return the (component) tags that are on the operation} */
    Set<String> operationTags();
}
// OperationType?????? Hmmm

record OperationPriority(OperationSite site, int priority) {}
