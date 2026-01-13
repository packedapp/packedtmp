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
package app.packed.web;

/**
 * Thrown when a web server fails to start because the configured port is already in use.
 */
public class PortInUseException extends RuntimeException {

    /** <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 1L;

    /** The port that was already in use. */
    private final int port;

    /**
     * Creates a new exception for the given port.
     *
     * @param port
     *            the port that was in use
     */
    public PortInUseException(int port) {
        super("Port " + port + " is already in use");
        this.port = port;
    }

    /**
     * Creates a new exception for the given port with the underlying cause.
     *
     * @param port
     *            the port that was in use
     * @param cause
     *            the underlying cause (typically a {@link BindException})
     */
    public PortInUseException(int port, Throwable cause) {
        super("Port " + port + " is already in use", cause);
        this.port = port;
    }

    /**
     * Returns the port that was in use.
     *
     * @return the port that was in use
     */
    public int getPort() {
        return port;
    }
}
