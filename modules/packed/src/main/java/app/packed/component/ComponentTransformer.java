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
package app.packed.component;

import java.lang.invoke.MethodHandles;

import app.packed.container.Assembly;

/**
 *
 */

// Application???
// Container
// Bean
// Operation
// Binding???
//// WebNamespace

// Can be applied as "ClientProxy" <-- Maybe this is Augmentation

// Can be applied as @BeanHook
// Can be applied as @AssemblyHook

//Can be applied as AssemblyTransformer



// Questions
//// 1. Stateless  (+ Locals) vs Statefull
//// 2. Pre/post? More control.
//// 3. Context or protected methods?

public abstract class ComponentTransformer {

    // Ville måske være godt at kunne få info ned...
    // Ellers må vi jo have oplysningerne på ContainerConfiguration og så tage den med.

    // Det ville være rigtig fedt at kunne se hvem der havde transformeret hvad

    // Det ville også være fint at have hvem og hvad seperaret
    public final Assembly transformRecursively(MethodHandles.Lookup caller, Assembly assembly) {
        return assembly;
    }

    // Ideen er lidt at vi kan define transformers der kan bruges paa beans...
    public static abstract class BeanLevelTransformer extends ComponentTransformer {

    }
}
