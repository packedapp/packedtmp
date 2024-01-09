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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import internal.app.packed.component.PackedComponentPath;

/** The schema of a component path. */
//https://backstage.io/docs/features/software-catalog/descriptor-format/
public interface ComponentKind {

    /** A component kind representing an application. */
    ComponentKind APPLICATION = builder(BaseExtension.class, "Application").requireString("application").build();

    /** A component kind representing a bean. */
    ComponentKind BEAN = builder().requireString("application").requirePath("containerPath").requireString("bean").build();

    /** A component kind representing a binding. */
    ComponentKind BINDING = builder().requireString("application").requirePath("containerPath").requireString("bean").requireString("operation")
            .requireString("binding").build();

    /** A component kind representing a container. */
    ComponentKind CONTAINER = builder().requireString("application").requirePath("containerPath").build();

    /** A component kind representing an operation. */
    ComponentKind OPERATION = builder().requireString("application").requirePath("containerPath").requireString("bean").requireString("operation").build();

    // We could always have one... Just make let BaseExtension own them..
    // And maybe skip base when printing the name
    Optional<String> extension();

    /** {@return the various fragments that make of the schema} */
    List<Map.Entry<String, ComponentKind.FragmentKind>> fragments();

    ComponentPath newPath(Object... fragments);

    String prefix();

    /** {@return a new schema builder} */
    static ComponentKind.Builder builder() {
        throw new UnsupportedOperationException();
    }

    static ComponentKind.Builder builder(Class<? extends Extension<?>> extensionType, String componentType) {
        throw new UnsupportedOperationException();
    }

    /** A builder for a component path schema. */
    sealed interface Builder permits PackedComponentPath.ComponentPathSchemaBuilder {

        /** {@return the new schema} */
        ComponentKind build();

        ComponentKind.Builder requireClass(String name);

        ComponentKind.Builder requireKey(String name);

        ComponentKind.Builder requirePath(String name);

        ComponentKind.Builder requireString(String name);
    }

    enum FragmentKind {
        CLASS, KEY, PATH, STRING;
    }
}