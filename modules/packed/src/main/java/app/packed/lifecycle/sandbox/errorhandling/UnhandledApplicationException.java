/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.lifecycle.sandbox.errorhandling;

import static java.util.Objects.requireNonNull;

import java.io.PrintStream;
import java.io.PrintWriter;

import app.packed.lifecycle.RunState;
import app.packed.operation.OperationInfoOld;

/**
 * An exception that is thrown when an application failed to execute.
 */
public class UnhandledApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The operation that failed. */
    private final OperationInfoOld operationSite;

    /** The run state of the application when it failed */
    private final RunState state;

    // Maybe OperationSite is optional.
    // For example, if I shutdown with a failure
    // ApplicationException. Maybe we cannot shut it down with a failure from the outside?

    public UnhandledApplicationException(RunState state, OperationInfoOld operationSite, String message) {
        super(message);
        this.state = requireNonNull(state);
        this.operationSite = requireNonNull(operationSite);
    }

    public UnhandledApplicationException(RunState state, OperationInfoOld operationSite, String message, Throwable cause) {
        super(message, cause);
        this.state = requireNonNull(state);
        this.operationSite = requireNonNull(operationSite);
    }

    public UnhandledApplicationException(RunState state, OperationInfoOld operationSite, Throwable cause) {
        super(cause);
        this.state = requireNonNull(state);
        this.operationSite = operationSite;
    }

    /** {@return the operation that failed} */
    public OperationInfoOld operationSite() {
        return operationSite;
    }

    /** {@return the run state of the application when it failed} */
    public RunState state() {
        return state;
    }

 // Main implementation in the PrintWriter version
    @Override
    public void printStackTrace(PrintWriter s) {
        Throwable rootCause = getCause();
        if (rootCause != null) {
            s.println("(" +UnhandledApplicationException.class.getName() + ".java:29) " + rootCause.toString());
            s.println("State: " + state);

            StackTraceElement[] trace = rootCause.getStackTrace();
            for (StackTraceElement element : trace) {
                s.println("\tat " + element);
            }
        } else {
            super.printStackTrace(s);
        }
    }

    // PrintStream version delegates to PrintWriter version
    @Override
    public void printStackTrace(PrintStream s) {
        // Create a PrintWriter that wraps the PrintStream
        PrintWriter pw = new PrintWriter(s, true); // true for auto-flush
        printStackTrace(pw);
        // No need to close the PrintWriter as it doesn't own the underlying stream
    }
}
