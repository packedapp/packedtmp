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

import static java.util.Objects.requireNonNull;

import app.packed.operation.OperationSite;
import app.packed.runtime.RunState;

/**
 * An exception that is thrown when an application failed to execute.
 */
public class ApplicationPanicException extends Exception {

    private static final long serialVersionUID = 1L;

    /** The operation that failed. */
    private final OperationSite operationSite;

    /** The run state of the application when it failed */
    private final RunState state;

    // Maybe OperationSite is optional.
    // For example, if I shutdown with a failure
    // ApplicationException. Maybe we cannot shut it down with a failure from the outside?

    public ApplicationPanicException(RunState state, OperationSite operationSite, String message) {
        super(message);
        this.state = requireNonNull(state);
        this.operationSite = requireNonNull(operationSite);
    }

    public ApplicationPanicException(RunState state, OperationSite operationSite, String message, Throwable cause) {
        super(message, cause);
        this.state = requireNonNull(state);
        this.operationSite = requireNonNull(operationSite);
    }

    public ApplicationPanicException(RunState state, OperationSite operationSite, Throwable cause) {
        super(cause);
        this.state = requireNonNull(state);
        this.operationSite = requireNonNull(operationSite);
    }

    /** {@return the operation that failed} */
    public OperationSite operationSite() {
        return operationSite;
    }

    /** {@return the run state of the application when it failed} */
    public RunState state() {
        return state;
    }
}
