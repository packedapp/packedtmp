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
package app.packed.bean;

import app.packed.operation.Op;

/** This enum represents the source type that was used installing a bean. */
public enum BeanSourceKind {

    /**
     * A {@link Class} was specified when creating the bean.
     * 
     * @see BeanKind#STATIC
     * @see BeanKind#CONTAINER
     * @see BeanKind#LAZY
     * @see BeanKind#MANYTON
     */
    CLASS,

    /**
     * A bean instance was specified when creating the bean.
     * 
     * @see BeanKind#CONTAINER
     */
    INSTANCE,

    /**
     * No source was specified when creating the bean.
     * 
     * @see BeanKind#FUNCTIONAL
     */
    NONE,

    /**
     * An {@link Op} was specified when creating the bean.
     * 
     * @see BeanKind#CONTAINER
     * @see BeanKind#LAZY
     * @see BeanKind#MANYTON
     */
    OP;
}
