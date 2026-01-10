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
package app.packed.telemetry.span;

import java.lang.ScopedValue.CallableOp;
import java.util.Optional;

import app.packed.telemetry.span.impl.PackedTracer;

/**
 *
 */
// Not closeable, wait for strong TWR
// https://github.com/open-telemetry/opentelemetry-java/blob/main/docs/rationale.md#span-not-closeable
public interface Span {


    default void endFailed(Throwable cause) {

    }

    default void end() {

    }
    /** {@return whether or not the span is active} */
    boolean isActive();

    /** {@return the name of the span} */
    String name();

    // current().newSpan("addfp");
    Span newSpan(String name);

    Span.Builder newSpanBuilder(String name);

    // I think the noop thing is
    static Span current() {
        return PackedTracer.SPAN.orElse(null);
    }

    /** {@return the current active span if one is present} */
    static Optional<Span> currentOptional() {
        return Optional.ofNullable(PackedTracer.SPAN.orElse(null));
    }

    static Span noop() {
        throw new UnsupportedOperationException();
    }

    // Altsaa noop, betyder jo at vi ikke kan bruge attributer til logging.
    static Span spawn() {
        return PackedTracer.SPAN.orElse(null);
    }

    /**
     * A builder for creating new {@link Span} instances.
     */
    interface Builder {

        <R, X extends Throwable> R call(CallableOp<? extends R, X> op) throws X;

        /**
         * Creates a new span and runs the action with the new span. Exiting the span whenever the action returns.
         *
         * @param action
         *            the action to run within the new span
         */
        void run(Runnable action);

        /**
         * Creates and starts the new span.
         *
         * @return the new span
         */
        Span start();
    }

    // Where does it get the tracer from??
    // I'm not sure this is current
//    static Span currentOrNew() {
//        throw new UnsupportedOperationException();
//    }
}
