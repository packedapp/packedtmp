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
package app.packed.bean.operation.nonruntime;

import java.util.Collection;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionSupport;

/**
 *
 */

// Hmm det er jo en del af et build... 
// Saa det er vel ikke en operation...
// F.eks.

// Spoergsmaal om det er mirror eller event...
// Nej vi vil godt kunne sige
public interface GeneratedClassMirror {

    // Eller ExtensionMirror extension(); .....
    Class<? extends Extension> extension(); // Eller kan en bruger generere en klasse

    /** {@return the class that was generated.} */
    Class<?> generatedClass();

    /** {@return whether or not the generated class is hidden.} */
    default boolean isHidden() {
        return generatedClass().isHidden();
    }
}

class ClassGeneratorExtension extends Extension {
    
    static class ClassGeneratorExtensionSupport extends ExtensionSupport {
        
    }
}

interface ClassGenExtensionMirror {

    // Returns a collection of all classes that was generated as part of the build
    /** {@return a collection of all classes that was generated as part of building the container.} */
    Collection<GeneratedClassMirror> generatedClasses();
    
    // Har vi andre interessant ting????
}

// FindAllGeneratedClass