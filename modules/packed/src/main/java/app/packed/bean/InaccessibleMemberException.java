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

/**
 * This exception is thrown when installing a bean, and the framework does not have reflective permissions to access one
 * or more members (constructor, field or method) of the bean.
 * <p>
 * In order to make the bean's member accessible, the right access must be provided to the framework. This can be done
 * either by opening the package in which the bean is located to {@value app.packed.util.FrameworkNames#BASE_MODULE}
 * using a module descriptor. Or by specifying a lookup object using {@link BuildableAssembly#lookup(Lookup)} or
 * {@link AbstractComposer#lookup(Lookup)}.
 */
public class InaccessibleMemberException extends BeanInstallationException {

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
    public InaccessibleMemberException(String message) {
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
    public InaccessibleMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
