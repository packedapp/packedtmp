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
// https://backstage.io/docs/features/software-catalog/descriptor-format/

//Selve component path schemaet skal bygges ind her
public class ComponentKind {

    /** A component kind representing an application. */
    public static final ComponentKind APPLICATION = null;

    /** A component kind representing a container. */
    public static final ComponentKind CONTAINER = null;

    /** A component kind representing a bean. */
    public static final ComponentKind BEAN = null;

    /** A component kind representing an operation. */
    public static final ComponentKind OPERATION = null;

    /** A component kind representing a binding. */
    public static final ComponentKind BINDING = null;

    /**
     * @param fragments
     *            the path fragments
     * @return a component path representing this component kind and the specified fragments
     *
     * @throws IllegalArgumentException
     *             if the fragments does not match the schema for this component kind
     */
    public ComponentPath pathOf(Object... fragments) {
        throw new UnsupportedOperationException();
    }
}
