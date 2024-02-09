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
package app.packed.build;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import app.packed.assembly.AssemblyMirror;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.component.Mirror;

/**
 *
 */

// Ideen er lidt at et program bestaar af en masse build instructions

// Maybe this only available in a streaming version??? Or maybe both...

// Can the user add Build instructions???????????? Why not

public interface BuildInstructionMirror extends Mirror {

    // The targeted assembly. For extensions this will for example be the container where the bean is installed in.
    AssemblyMirror assembly();

    String name();

    Method sourceMethod(); // Is actual method Yeah Probably

    Method sourceMethodName(); // fx afterBuild(AssemblyConfiguration configuration)

    // What about delegating assemblies here? Maybe better with an actual mirror

    // I think it would be nice to maybe track how we got here...
    Class<? extends BuildSource> sourceType(); // actual type do need more
}

interface ZExtras {

    // LoadConfigurationFile...
    ComponentMirror component(); // Optional??? For example, user. Or Set???

    Set<ComponentPath> components(); // Alternative to returning the actual mirror

    String message(); // ??

    Map<String, String> properties(); // fx BeanName? IDK
}

// bean.install; source = SomeAssembly, sourceMethodName = build
// bean.install; source = SomeAssembly, sourceMethodName = build
// bean.install; source = SomeContainerTransformer, sourceMethodName = onNew