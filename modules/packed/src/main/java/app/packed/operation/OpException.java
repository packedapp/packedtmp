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
package app.packed.operation;

import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * This exception is thrown when an op could not be created. For example, because a valid constructor or method could
 * not be found.
 */

// TypeVariable
// Ved ikke om vi skal smide den naar vi ikke kan finde

//// 2 situation
// Creation time. Primaert capturing. Syntes bare vi skal smide I

public class OpException extends RuntimeException {

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
    public OpException(String message) {
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
    public OpException(String message, Throwable cause) {
        super(message, cause);
    }

    public static void main(String[] args) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");

        // Sort all system properties
        List<Entry<Object, Object>> sorted = System.getProperties().entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().toString().compareTo(e2.getKey().toString())).collect(Collectors.toList());

        // Add them to String joiner, removing new line so it is all on one line
        sorted.forEach(e -> sj.add(e.getKey() + "=" + e.getValue().toString().replace("\n", "").replace("\r", "")));

        // Print to console
        System.out.println("System.properties -> " + sj.toString());
    }
}
