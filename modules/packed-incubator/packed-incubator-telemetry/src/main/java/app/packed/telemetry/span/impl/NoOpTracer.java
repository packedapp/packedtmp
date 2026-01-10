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
package app.packed.telemetry.span.impl;

import java.lang.ScopedValue.CallableOp;

import app.packed.telemetry.span.Span;
import app.packed.telemetry.span.Span.Builder;

/**
 *
 */
public final class NoOpTracer extends AbstractTracer {

    public static final NoOpTracer INSTANCE = new NoOpTracer();

    private NoOpTracer() {}

    /** {@inheritDoc} */
    @Override
    public Builder newSpan(String name) {
        return null;
    }

    public static final class NoOpSpanBuilder extends AbstractSpanBuilder {

        /** {@inheritDoc} */
        @Override
        public <R, X extends Throwable> R call(CallableOp<? extends R, X> op) throws X {
            return op.call();
        }

        /** {@inheritDoc} */
        @Override
        public void run(Runnable action) {
            action.run();
        }

        /** {@inheritDoc} */
        @Override
        public Span start() {
            return null;
        }
    }
}
