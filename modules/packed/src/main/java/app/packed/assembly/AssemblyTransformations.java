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
package app.packed.assembly;

import java.lang.invoke.MethodHandles;

import app.packed.bean.BeanTransformer;
import app.packed.component.ComponentTransformer;
import app.packed.container.ContainerTransformer;

/**
 *
 */


// Vi vil jo hellere sige ignorer installation af denne bean
// Eller erstat bean classes den af den her...

public final class AssemblyTransformations {

    /**
     * @param caller
     * @param assembly
     * @return
     * @throws IllegalArgumentException
     *             if the specified assembly is in use or has already been used
     */
    public static Assembly transformRecursively(MethodHandles.Lookup caller, Assembly assembly, ContainerTransformer transformer) {
        return assembly;
    }

    public static Assembly transformRecursively(MethodHandles.Lookup caller, Assembly assembly, BeanTransformer transformer) {
        return assembly;
    }

    // Hvis du er en lifecycle operation i container X. Så gør foo
    public class OperationTransformer implements ComponentTransformer {

    }
}
