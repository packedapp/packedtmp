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
 * An exception typically thrown by the runtime to indicating an internal malfunction of an extension.
 * <p>
 * If you encounter this exception as the end-user of an extension. There is typically nothing you can do, except for
 * reporting the failure to the developer of the extension.
 */
public class InternalExtensionException extends RuntimeException {

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception.
     * 
     * @param message
     *            the message
     */
    public InternalExtensionException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public InternalExtensionException(String message, Throwable cause) {
        super(message, cause);
    }
}
