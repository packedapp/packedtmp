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

import app.packed.extension.BaseExtension;

/**
 * An exception thrown when a bean is being installed, and there is another bean in the same container with the same
 * bean class.
 * <p>
 * Installation of beans with a {@code void} bean class (functional beans) never fails.
 * 
 * @see BaseExtension#multiInstall(Class)
 * @see BaseExtension#multiInstall(app.packed.operation.Op)
 * @see BaseExtension#multiInstallInstance(Object)
 */
public class BeanClassAlreadyExistsException extends BeanInstallationException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new exception with the specified detailed message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link Throwable#initCause}.
     *
     * @param message
     *            the detailed message. The detailed message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public BeanClassAlreadyExistsException(String message) {
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
    public BeanClassAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
