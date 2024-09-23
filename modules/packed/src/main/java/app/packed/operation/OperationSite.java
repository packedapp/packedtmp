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
public interface OperationSite {
    Class<?> beanClass();
    ComponentPath beanComponentPath();
    String beanName();
    ComponentPath containerComponentPath();
    String containerName();
    String containerPath();
    Set<? extends Context<?>> contexts();
    ComponentPath operationComponentPath();
    String operationName();

    Set<String> tags();
}
