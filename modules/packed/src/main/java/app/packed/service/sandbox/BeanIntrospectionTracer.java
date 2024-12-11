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
package app.packed.service.sandbox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 */
// It's not an operation...
// It can be used together with Variable...
// It can neither set nor get
// We want it triggered before anything else.
// It should also work on services...

// Maybe Variable and OnField is only a problem when #set = true
// On the other hand I want to have potential two annotations

// Boer vel ogsaa kunne specificere denne som en BuildWirelet
@Target({ ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
public @interface BeanIntrospectionTracer {
    boolean details() default false;

    // Useful if on Assembly
    Class<?>[] beanClasses() default {}; //anyOf (AssignableTo)

    String[] memberFilter() default {}; // regexp anyOf
}
