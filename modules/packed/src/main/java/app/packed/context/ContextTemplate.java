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
package app.packed.context;

import java.util.List;
import java.util.Set;

import app.packed.bean.BeanIntrospector.OnBinding;
import app.packed.service.Key;

/**
 * A template
 */
// Featuress

// ContextClass

// InternalContextClass used when invoking <- Maybe actually an array...

// Keys available -> MethodHandle extractor from parameters

// Det er jo en slags ContextModel??

// Kan ogsaa require argumenter fra OperationContext...
// ExtensionBean er jo oplagt...
public class ContextTemplate {

    // Is SchedulingContext available for injection
    boolean isContextAvailableAsKey() {
        return false;
    }
    
    // Tror vi bliver noedt til at definere om vi operere 
    // i en operation eller freeform (SessionContext)
    
    public List<Class<?>> invocationArguments() {
        return List.of();
    }

    Class<? extends Context<?>> contextClass() {
        throw new UnsupportedOperationException();
    }

    /** {@return keys that are available for the context.} */
    public Set<Key<?>> keys() {
        return Set.of();
    }

    // IDK
    protected void notInContext(OnBinding dep) {

    }

    public static ContextTemplate of(Class<? extends Context<?>> contextClass) {
        throw new UnsupportedOperationException();
    }
}
// requireContext