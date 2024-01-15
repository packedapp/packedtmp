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
package app.packed.bean;

import app.packed.build.BuildException;

/**
 * An exception that is typically thrown when trying to install a stateless (static or foreign) bean that makes use of
 * functionality that is not available for stateless beans. For example, lifecycle annotations or non-static operation
 * methods on static beans.
 *
 */

// Syntes det er okay at smide for stateless ogsaak

// Hmm maaske saa bare have en UnmanagedBeanInstallationException
// Container smider en Unsupported operation exception vil jeg mene.
// Hvis man proever at installere en managed container i en unmanaged container

// Hvad hvis vi siger .container.runOnShutdown(Runnable) <---

// LifetimeNotManagedException. The lifetime of x bean is not managed by the container after it has been created
// Must rely on GC

// BeanNotManagedException?
// IDK kan vi bruge den til containere?


// Replace managed with whatever we end up
public class ManagedBeanRequiredException extends BuildException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the specified detailed message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link Throwable#initCause}.
     *
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public ManagedBeanRequiredException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified detailed message and cause.
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()}method).
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public ManagedBeanRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}