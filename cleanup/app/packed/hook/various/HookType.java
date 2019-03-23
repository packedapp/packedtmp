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
package app.packed.hook.various;

/** The various type of hooks. */
public enum HookType {

    /** A field annotated with a particular annotation. */
    ANNOTATED_FIELD,

    /** A method annotated with a particular annotation. */
    ANNOTATED_METHOD,

    /** A type annotated with a particular annotation. */
    ANNOTATED_TYPE,

    /** A type assignable to a particular type. */
    TYPE;
}
