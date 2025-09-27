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
package app.packed.container;

/**
 * This exception is typically through in one of the following situations:
 *
 * Attempting to launch an application or lifetime with a wirelet that does not have the launch phase as targetted phase.
 *
 * A wirelet was specified either at build time or at launch time that had no consumers.
 *
 * Wirelet specified but not appropriated for specific use site (ApplicationWirelet on leaf container)
 */
/**
 * Indicates that a specified wirelet was not used anywhere. This can happen, for example, if a wirelet for a specific
 * extension is specified but the extension itself is never used.
 * <p>
 * The reason for always checking that a wirelet has been used, is to fail-fast and avoid frustrating users more than
 * necessary.
 */
// Hmm maybe a generic wirelet exception?
// *) Wirelet specified but no consumers. (UnconsumedWireletException) (UnusedWireletException)
// *) Wirelet specified but not appropiated for specific use site (ApplicationWirelet on leaf container)

// *) BuildException, try to inject WireSelection<SomeApplicationWirelet> for an extensionBean

// Was BuildTimeWireletNotAllowedException
// LaunchTimeWireletRequiredException
public class WireletException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the specified detailed message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link Throwable#initCause}.
     *
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public WireletException(String message) {
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
    public WireletException(String message, Throwable cause) {
        super(message, cause);
    }
}
