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

/**
 * 
 */

// Altsaa taenker at vi fx kan faa en Kotlin faetter...
// Er ikke super vild med en enum her
public enum ComponentSourceType {

    CLASS,

    /** A functional interface. */
    FUNCTION,

    // Altsaa vi skal jo angive hvor @Provide X kommer fra saa selvfoelgelig har vi brug for wirelets.
    // Som jeg ser det 
    WIRELET; // ??? Jeg taenker paa at vi har brug for den...
}
// 3 attributer eller en??? COMPONENT_SOURCE_TYPE, 
//COMPONENT_SOURCE_CLASS_TYPE
//COMPONENT_SOURCE_FUNCTION_TYPE
//COMPONENT_SOURCE_WIRELET_TYPE

// Template...

// JPA -> Class... 