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
package archive.bean.dependency;

import app.packed.bean.BeanExtensionPoint.VariableBindingHook;
import app.packed.container.MirrorExtension;

/**
 *
 */
@VariableBindingHook(extension = MirrorExtension.class)
// Hmm man kan jo ikke bare ignorere denne.... Hvis man har lyst...
// Den kan kun bruges paa dependencies, ikke fx @Provide someField
// Men hvad med de steder @Provide bliver brugt??? 

// Den er vel interessant begge veje. Hvem bruger jeg, og hvem bruger mig
// Eller maaske er de seperate?
public interface DependencyTracker<T> {
    T get();
    
    // print graph
}
