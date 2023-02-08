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
package app.packed.bindings;

import app.packed.application.BuildException;

/**
 * An exception thrown at build time to indicate a dependency cycle between multiple service providers.
 */
// Findes der andet end service dependencies cirkler?
// Hvis man ikke kan bruge sine egne annoteringer
public class CircularDependencyException extends BuildException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the specified detailed message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link Throwable#initCause}.
     *
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    // Super useful to provide the cycle I think. Don't know when we get to cross container cycles
    // Then you can just do what ever you want it e.printBeans()
    public CircularDependencyException(String message /*, List<ServiceProvisionMirror> cycle */) {
        super(message);
    }
}
