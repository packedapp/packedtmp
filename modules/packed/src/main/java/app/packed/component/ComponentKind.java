/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import internal.app.packed.component.PackedComponentKind;
import internal.app.packed.component.PackedComponentKind.PackedComponentKindBuilder;

/** The type of a component. */
//https://backstage.io/docs/features/software-catalog/descriptor-format/

// Application=FileSystem
// Container = Folder
// Bean = Files
// Operation = Line in the file
// Binding = Letter on the line
public sealed interface ComponentKind permits PackedComponentKind {

    /** A component kind representing an application. */
    ComponentKind APPLICATION = builder("Application", BaseExtension.class, "Application").requireFragmentString("application").build();

    /** A component kind representing a binding. */
    ComponentKind NAMESPACE = builder("Namespace").requireFragmentString("application").build();

    /** A component kind representing a container. */
    ComponentKind CONTAINER = builder("Container").requireFragmentString("application").requireFragmentPath("containerPath").build();

    /** A component kind representing a bean. */
    ComponentKind BEAN = builder("Bean").requireFragmentString("application").requireFragmentPath("containerPath").requireFragmentString("bean").build();

    /** A component kind representing an operation. */
    ComponentKind OPERATION = builder("Operation").requireFragmentString("application").requireFragmentPath("containerPath").requireFragmentString("bean")
            .requireFragmentString("operation").build();



    // We could always have one... Just make let BaseExtension own them..
    // And maybe skip base when printing the name
    Optional<String> extension();

    /** {@return the name of the component kind} */
    String name();

    /** {@return the various fragments that make of the schema} */
    List<Map.Entry<String, ComponentPath.FragmentKind>> pathFragments();

    /**
     * @param parent
     *            the path of the parent component if this component kind was defined with such a parent
     * @param fragments
     * @return
     */
    ComponentPath pathNew(ComponentPath parent, Object... fragments);

    ComponentPath pathNew(Object... fragments);

    // vs NAme??
    String pathPrefix();

    /** {@return a new component kind builder} */
    static ComponentKind.Builder builder(String name) {
        return new PackedComponentKindBuilder(name, BaseExtension.class);
    }

    static ComponentKind.Builder builder(String name, Class<? extends Extension<?>> extensionType, String componentType) {
        return new PackedComponentKindBuilder(name, extensionType);
    }

    // I think everyone except base components must have a parent component
    // So all components are formed as a tree. With the application as the root...
    static ComponentKind.Builder builder(String name, ComponentKind parent, Class<? extends Extension<?>> extensionType, String componentType) {
        throw new UnsupportedOperationException();
    }

    /** A builder for a component kind. */
    interface Builder  {

        /** {@return the new component kind} */
        ComponentKind build();

        ComponentKind.Builder requireFragmentClass(String name);

        ComponentKind.Builder requireFragmentKey(String name);

        ComponentKind.Builder requireFragmentPath(String name);

        ComponentKind.Builder requireFragmentString(String name);
    }
}
//
///** A component kind representing a binding. */
//ComponentKind BINDING = builder("Binding").requireFragmentString("application").requireFragmentPath("containerPath").requireFragmentString("bean")
//      .requireFragmentString("operation").requireFragmentString("binding").build();
