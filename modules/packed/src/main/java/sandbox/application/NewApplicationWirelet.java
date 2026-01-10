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
package sandbox.application;

import app.packed.container.Wirelet;

/**
 *
 */
// Lazy taenker jeg er shared med application
public class NewApplicationWirelet {

    // Taenker ogsaa W.overrideAllLazy (beans, containers, applications)

    // Kan bruges til at override fork/join build wirelets og lazy build.
    // Er ogsaa i test

    // Fx hvis man vil bygge et ApplicationMirror hvor alt er med
    public static Wirelet forceBuildSync() {
        throw new UnsupportedOperationException();
    }

    // Taenker man maa dable i ApplicationTemplate hvis man vil lave ny apps...
    public static Wirelet newApplication() {
        throw new UnsupportedOperationException();
    }

    // Builds the application in its own thread
    // join = wait on the mother fucker

    // Hvordan klare man mirrors hvis applikationen bliver bygget
    // Maa vaere via en ApplicationBridge. Som er en bean
    // Eller ogsaa er den bare altid som hvis man vaelger at bygget
    public static Wirelet forkBuild(boolean join) {
        throw new UnsupportedOperationException();
    }
}
