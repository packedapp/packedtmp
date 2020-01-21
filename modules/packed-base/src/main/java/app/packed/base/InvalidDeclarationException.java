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
package app.packed.base;

/**
 * A runtime exception that is typically thrown when the runtime encounters a structural problems on some object. For
 * example, a final field annotated with {@link app.packed.inject.Inject}.
 */
// ComponentDeclarationException.
// Skal vel kende forskel paa runtime og build time...
// Skal vel ogsaa flyttes til component packen.

// ClassDefinitionException...
// ConstCreateException.. <- Det er en exception der bliver lavet naar man bruger const api'en...
public class InvalidDeclarationException extends RuntimeException {

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
    public InvalidDeclarationException(CharSequence message) {
        super(message.toString());

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
    public InvalidDeclarationException(CharSequence message, Throwable cause) {
        super(message.toString(), cause);
    }
}
// Kunne vaere fedt med et configuration site...Men nah er det ikke kun statisk vi brokker os over