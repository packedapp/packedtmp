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
package app.packed.telemetry;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.telemetry.span.Span;
import internal.app.packed.ValueBased;

/**
 *
 */
@ValueBased
public interface Logger {

    void ifInfo(Consumer<? super Logger> action);

    void info(String message);

    void info(Supplier<? super String> message);

    /** {@return the name of the logger} */
    String name();

    /** {@return any span that is valid} */
    Optional<Span> span(); // Maybe it is no-op

    // Or withAttribute? I think
    /**
     * Creates a new logger with the specified attribute applied.
     * <p>
     *
     * @param attribute
     *            the name of the attribute
     * @param value
     *            the value of the attribute
     * @return the new logger
     * @implNote this is a cheap operation
     */
    // Det der er daarligt her er at vi bruger tid selvom det ikke skal logges
    // atInfo() er saa meget bedre.
    Logger withAttribute(String attribute, String value);

    // If we log with exceptions, we should probably automatically
    // set the exception if a span exists.
    // No not after we ditched AutoClosable

    Builder atInfo();

    interface Builder {

        Builder withAttribute(String attribute, String value);

        void log(String message);
    }
}
