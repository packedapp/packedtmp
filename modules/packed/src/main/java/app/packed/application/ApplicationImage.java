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
package app.packed.application;

/**
 * A
 */
public interface ApplicationImage extends ApplicationLauncher {

    /** {@return a mirror of the application} */
    ApplicationMirror mirror();

    /** {@return the name of the application} */
    String name();

    /**
     * Prints the structure of the application to {@code System.out}.
     * <p>
     * This is a convenience method for debugging and analysis purposes.
     */
    default void print() {
        mirror().printer().print();
    }
}
