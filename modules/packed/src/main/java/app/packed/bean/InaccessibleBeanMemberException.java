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

import app.packed.application.BuildException;

// An exception that is thrown when a operation could not be created because there was no access to the underlying.
// Field, Constructor or Method
/** A runtime exception used in places where we cannot throw the checked {@link IllegalAccessException}. */
// UncheckedIllegalAccessException...
// RuntimeIllegalAccessException

// AccessRestrictedException <- General one, could sound really securish, maybe have a name
// which makes it clear it is relevant to reflection/method handlers
// NotOpenedException
// UndeclaredAccessException
// Was UncheckedIllegalAccessException

// InaccessibleRealmException or
// InaccessibleModuleException

// Maybe it is a build exception??? Skal jo helst klare det under build..

// InaccessibleOperationException???
// InaccessibleBeanException??? Vil ogsaa godt bruge den fra Extension som ikke er en bean

// FactoryAccessException?? Nahh det er jo ikke sikkert vi overhoved skal lave en instance.
// saa factory er et daarligt navn/

// beanClass, Extension that needed access
public class InaccessibleBeanMemberException extends BuildException {

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
    public InaccessibleBeanMemberException(String message) {
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
    public InaccessibleBeanMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
