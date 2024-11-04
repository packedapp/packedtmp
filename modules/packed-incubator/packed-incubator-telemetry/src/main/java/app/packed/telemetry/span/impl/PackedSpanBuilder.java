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
package app.packed.telemetry.span.impl;

import java.lang.ScopedValue.CallableOp;

import app.packed.telemetry.span.Span;

/**
 *
 */
public final class PackedSpanBuilder extends AbstractSpanBuilder {

    /** {@inheritDoc} */
    @Override
    public <R, X extends Throwable> R call(CallableOp<? extends R, X> op) throws X {
        Span s = start(); // Assume this initializes and starts a span for tracing
        R result;
        try {
            result = op.call(); // Execute the operation
        } catch (Throwable throwable) {
            s.endFailed(throwable); // Mark the span as failed
            throw throwable;
        }
        s.end(); // Mark the span as successful
        return result; // Return the result of the operation
    }

    /** {@inheritDoc} */
    @Override
    public void run(Runnable action) {
        Span s = start(); // Assume this initializes and starts a span for tracing
        try {
            action.run();
        } catch (Throwable throwable) {
            s.endFailed(throwable); // Mark the span as failed
            throw throwable;
        }
        s.end(); // Mark the span as successful
    }

    /** {@inheritDoc} */
    @Override
    public Span start() {
        return null;
    }
}
