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
package app.packed.sidecar;

import app.packed.base.Key;

/** This enum contains the various ways a sidecar can be activated. */
public enum SidecarActivationType {
    
    /** Activated by a specific annotation on a class. */
    ANNOTATED_CLASS,
    
    /** Activated by a specific annotation on a field. */
    ANNOTATED_FIELD,
    
    /** Activated by a specific annotation on a method. */
    ANNOTATED_METHOD,
    
    /** Activated by a specific annotation on a variable (field, parameter or type variable). */
    ANNOTATED_VARIABLE,
    
    /** Activated by a dependency represented via a {@link Key}. */
    DEPENDENCY;
}
// Annotated Variable can only provide a dependency
// Cannot have more than a single of such sidecars on a variable
