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

import internal.app.packed.component.PackedComponentPath;

// Goals
//// Uniquely identify a component

//// Supports both framework resources and extension resources

//// We only support static resource

//// Schema that explains the path and each Fragment

// Non-Goals

//// Supporting User defined Resources (only framework,extension)
//// Support Resources at runtime (Everything is fixed at build time)
//// Creating a path without a Schema. (We would need a very different architecture)

// Maybe
//// Supports resolve??

// Issues
//// Renaming -> Den maa bare faa et nyt navn

// Vi har trees (containers)
// Vi har lists (beans)

// (Vi har ting der er svaere at navngive)
// Extensions -> SimpleName, ved sammenfald maa vi bruge 1,2,3

// Maaske har vi baade en long path of en short path.
// Fx for extensions og andre ting med klasse navn

/**
 * Taenker den her er uundvaergelig naar vi exporter/importer som en streng
 */
// Application:appName:
// Container:appName::Foobar Container:appName:/FooBar
// Assembly:appName:/,  Assembly:appName:/MyAssembly
// Bean:appName:/:MyBean,  Bean:appName:/MyAssembly:BooBean
// Operation:appName:/:MyBean:getStuff
// Binding:appName:/:MyBean:getStuff:0
// Extension:appName:Container:ExtensionName (SimpleClassName uniqiefied

// Names, Classes, Keys, ...

//// what about customized exports??? Det kan jo vaere den samme service
//// Er det renames? Adaptors? idk.
// Service:appName:Container

// Problemet er alt med et class name...
///// Context fx

// Context:

// Cli.Foo:

///////
// Namespace:appName:/??? Tror vi har

// Was ResourcePath
// ComponentMirro = { ComponentPath path(); }
/**
 * The path of a component.
 * <p>
 * All components within a single application has a unique path.
 *
 */
public sealed interface ComponentPath permits PackedComponentPath {

    /** {@return the schema of the path.} */
    ComponentKind componentKind();

    default String fragment(int index) {
        throw new UnsupportedOperationException();
    }

    default String fragment(String name) {
        throw new UnsupportedOperationException();
    }

    // Problemet med at sige nameFragment.
    // Er hvis vi ikke gemmer Class instances.
    // Saa giver det ingen mening at kunne returnere de andre fragmenter.
    default String nameFragment(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param fragmentName
     *            the name of the fragment
     * @return a string representation of the fragment with the specified name
     * @throws IllegalArgumentException
     *             if a fragment with the specified name does not exist
     */
    default String nameFragment(String fragmentName) {

        // path.nameFragment("bean");
        throw new UnsupportedOperationException();
    }

    default List<String> pathFragment(int index) {
        throw new UnsupportedOperationException();
    }
}
