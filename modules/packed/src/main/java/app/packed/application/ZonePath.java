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
 *
 */
// Id?
public interface ZonePath {

    static ZonePath ofApplication(String applicationName) {
        return null;
    }

    static ZonePath ofContainer(String applicationName, String[] containers) {
        return null;
    }

    static ZonePath ofAssembly(String applicationName, String[] assemblies) {
        return null;
    }

    static ZonePath ofOperation(String applicationName, String[] containers, String bean, String operation) {
        return null;
    }

    static ZonePath ofBinding(String applicationName, String[] containers, String bean, String operation, int[] bindings) {
        return null;
    }

    static ZonePath ofBean(String applicationName, String[] containers, String bean) {
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