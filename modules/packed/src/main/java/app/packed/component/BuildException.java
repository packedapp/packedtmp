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
package app.packed.component;

/**
 * An exception that is typically thrown when build phase of an application fails.
 * <p>
 * This exception normally indicates a programmatic error and can usually only be recovered by updates to the underlying
 * code.
 */
// Taenker configurations fejl maaske smider ConfigException - Det kan jo baade vaere paa runtime og build time
// ApplicationBuildException (Men saa lukker vi for at vi kan deploye adhoc ting
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
//Or in lifecycle???? or base??? Hmmmmmm
//Hed noget med artifact foer, men hvis vi faar dynamiske componenter...
// InitializationException
// ExecutionException (vi har den jo i juc...)
//Skal vi have en liste af errors??? 
//Evt i suppresed exceptions???
//Skal vi have en lifecycle base extension for denne og InitializationExtception  
