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
package app.packed.application;

import java.util.List;

/**
 * Taenker den her er uundvaergelig naar vi exporter/importer some en streng
 */
// Application:appName:
// Container:appName::Foobar Container:appName:/FooBar
// Assembly:appName:/,  Assembly:appName:/MyAssembly
// Bean:appName:/:MyBean,  Bean:appName:/MyAssembly:BooBean
// Operation:appName:/:MyBean:getStuff
// Binding:appName:/:MyBean:getStuff:0
// Extension:appName:Container:ExtensionName (SimpleClassName uniqiefied)

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

public interface DeploymentPath {

    /**
     * {@return a path representing an application with the specified name}
     *
     * @param applicationName
     *            the name of the application
     */
    static DeploymentPath ofApplication(String applicationName) {
        return null;
    }

    static DeploymentPath ofAssembly(String applicationName, String[] assemblies) {
        return null;
    }

    static DeploymentPath ofBean(String applicationName, String[] containers, String bean) {
        return null;
    }

    static DeploymentPath ofBinding(String applicationName, String[] containers, String bean, String operation, int[] bindings) {
        return null;
    }

    /**
     * {@return a path representing a container}
     *
     * @param applicationName
     *            the name of the application
     * @param container
     *            list of
     */
    static DeploymentPath ofContainer(String applicationName, String[] containerNames) {
        return null;
    }

    static DeploymentPath ofExtension(String applicationName, String[] containers, String extension) {
        return null;
    }

    static DeploymentPath ofOperation(String applicationName, String[] containers, String bean, String operation) {
        return null;
    }

    interface ComponentPathModel {
        List<String> fragments();

        boolean isCustom();

        String name();

        interface Fragment {
            String name();

            Class<?> type();
        }
    }

    enum FragmentKind {
        NAME, PATH
    }

    enum Kind {
        APPLICATION, ASSEMBLY, BEAN, CONTAINER;
    }
}
