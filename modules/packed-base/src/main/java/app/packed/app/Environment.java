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
package app.packed.app;

/**
 * Exactly one environment exist in a single ClassLoader. Normally this is also one per JVM. But you can easily load
 * packed in multiple class loaders. In which case they each get their own environment.
 */
// app.packed.environment... (maaske base alligevel....)
// Tror vi skal

// Disable stack trace gathering.... //Should be able to write some
// disable stack trace gathering in the following environments...
// Should be able to do it on App level....

//// Always just one environment, but with multiple string based profiles...
// Profiles is _JUST_ STRINGs
public final class Environment {

    // reboot (Mest taenkt i test, maaske kun i devtools)
    // initialize
    // destroy... dead

    public static Environment instance() {
        throw new UnsupportedOperationException("Is instance variables really needed");
    }

    // Is initialized before any bundles are processed (That way it should be as secure as possible)
    // Libraries must not attempt to initialize it in any way (Maybe we just don't allow it)
    // Use a BaseEnvironment class.....
    // Single Instance

    // Det skal vaere muligt at definere extensions der kan loades nogle der kan loades..

    public static void initializeWith(/* BaseEnvironment baseEnvironment */) {
        // Ideen er at vi overskriver det default Environment, og hvad der evt er paa command line, so

        // Default (Via ServiceLoader, only on per instance) (Do we have an overriddable...) Maas
        // Denne kan tage en overriddable, hvis den sk
        // Overridden

        // Default
        // Via System.properties
        // Manual Override

        // Er ikke overskrevet af graal. Men bliver gemt....
    }
}
// reboot (will reinitialize everything) read System properties again.
/// The initalization list will be cleared every time we create an environment.
// destroy

// One environment, multiple profiles <--- All statically fixed cannot be changed....