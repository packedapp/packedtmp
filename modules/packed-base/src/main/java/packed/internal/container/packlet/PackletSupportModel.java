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
package packed.internal.container.packlet;

import java.util.Map;

/**
 * A packlet support model can be added to container configurations (via an annotation on a bundle) or component
 * configuration (via an annotation on the actual component)
 */
public class PackletSupportModel {

    /** All entries. */
    final Map<Class<?>, Entry> entries = null;

    PackletSupportModel(Builder builder) {

    }

    // En annoteringer kan pege på flere packlets og/eller sidecars

    // En component

    // Kan man bygge den paa en anden maade
    // Og vi skal vel bruge den
    static class Entry {
        // Packlet Class
        // Openings
        // ExtensionModel
        // SidecarModel...
    }

    static class Builder {

    }
}
