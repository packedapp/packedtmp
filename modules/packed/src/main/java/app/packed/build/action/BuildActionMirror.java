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
package app.packed.build.action;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.assembly.AssemblyMirror;
import app.packed.build.BuildCodeSourceMirror;
import app.packed.build.Mirror;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;

/**
 *
 */

// Ideen er lidt at et program bestaar af en masse build instructions

// Maybe this only available in a streaming version??? Or maybe both...

// Can the user add Build instructions???????????? Why not
// BuildAction?
public interface BuildActionMirror extends Mirror {

    // The assembly in which the action was performed.
    // The targeted assembly. For extensions this will for example be the container where the bean is installed in.
    // Root action, Assembly.build <--- Empty or just self reference??
    // For link I think we want the parent (link)? Maybe it is 2 actions. Link + Build
    AssemblyMirror assembly();

    /** {@return The authority on which behalf the action is performed} */
    ComponentRealm authority();

    boolean isNested();

    // Maa ogsaa have noget target (component)
    String name();

    // Do we need more here? Would it nice to understand for example, how a BuildTransformer ended up here?
    /** {@return the build source} */
    BuildCodeSourceMirror source();

    Optional<Method> sourceMethod(); // Is actual method Yeah Probably

    // Er vel ikke optional. Vi ved vel altid source name???
    Optional<String> sourceMethodName(); // fx afterBuild(AssemblyConfiguration configuration)

    // What about delegating assemblies here? Maybe better with an actual mirror
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

// bean.install: authority = Application, executor = BaseExtension