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

/**
 * A generic build exception that may be thrown when building an application.
 * <p>
 * This exception typically indicates a programmatic error that can usually only be recovered by updates to the
 * underlying code or configuration files.
 */
// Taenker configurations fejl maaske smider ConfigException - Det kan jo baade vaere paa runtime og build time

// Vi wrapper ikke altid i BuildException. Giver ikke mening fx at fx wrappe NPE. Det goer Guice og det er 
// aerlig talt mere besvaerligt at laese hvad der sker end bare et simpelt stacktrace

// PackedException, PackedBuildException, PackedRuntimeException
// Haha, hvad med ConfigException, den kan baade smides paa runtime og paa build time
// IDK. Maaske drop den
// ApplicationBuildException
public class BuildException extends RuntimeException {

    /** <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the specified detailed message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link Throwable#initCause}.
     *
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public BuildException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified detailed message and cause.
     *
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()}method). (A{@code null} value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public BuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
//ApplicationBuildException (Men saa lukker vi for at vi ikke kan deploye beans adhoc)...
//Det er maaske fint. Syntes helt sikkert vi skal omnavngive den hvis vi kun bruger den til 
//applications.
// Det er jo stadig en slags build af en application. Bare runtime extension
