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
// Application:appName
// Container:appName:/ Container:appName:/FooBar
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

public interface ApplicationPath {

    static ApplicationPath ofApplication(String applicationName) {
        return null;
    }

    static ApplicationPath ofContainer(String applicationName, String[] containers) {
        return null;
    }

    static ApplicationPath ofAssembly(String applicationName, String[] assemblies) {
        return null;
    }

    static ApplicationPath ofOperation(String applicationName, String[] containers, String bean, String operation) {
        return null;
    }

    static ApplicationPath ofBinding(String applicationName, String[] containers, String bean, String operation, int[] bindings) {
        return null;
    }

    static ApplicationPath ofBean(String applicationName, String[] containers, String bean) {
        return null;
    }

    static ApplicationPath ofExtension(String applicationName, String[] containers, String extension) {
        return null;
    }

    enum Kind {
        APPLICATION, CONTAINER, BEAN, ASSEMBLY;
    }

    enum FragmentKind {
        NAME, PATH
    }
}

interface ComponentPathModel {
    String name();

    boolean isCustom();

    List<String> fragments();

    interface Fragment {
        String name();

        Class<?> type();
    }
}