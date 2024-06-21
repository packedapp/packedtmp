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
package app.packed;

import java.util.Set;

/**
 * This class contains various names of extensions and modules that can be used as constants in code to avoid loading
 * various classes at runtime.
 *
 */
// Fx kan det vaere svaert at lave Application layers...
// Hvis man ikke gider importere alle pakker

// Om vi smider det hele her... Eller paa noget $Extension$Constants klasse afgoeres jo nok om vi ta
// Honestly throw it on Framework...

// FrameworkConstants? On the other hand we would then need to append NAME on all of them
public final class FrameworkNames {

    // It is important to have it as a string..
    // But then you cannot change the name of the framework, easily...
    // Cannot have it both ways

    /** The name of the framework. */
    public static final String FRAMEWORK = "Packed";

    /** The name of the module that defines the framework. */
    public static final String MODULE_BASE = "app.packed";

    /** The name of the devtools module. */
    public static final String MODULE_BASE_DEVTOOLS = "app.packed.devtools";

    /** The name of the devops module. */
    public static final String MODULE_BASE_DEVOPS = "app.packed.devops";

    static final String MODULE_WEB = "app.packed.web";

    /** A set of module names that makes of the framework. (So far there is only one.) */
    static final Set<String> ALL_FRAMEWORK_MODULES = Set.of(MODULE_BASE);

    private FrameworkNames() {}
}
