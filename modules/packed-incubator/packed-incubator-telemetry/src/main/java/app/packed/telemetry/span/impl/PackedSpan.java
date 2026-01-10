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

import static java.util.Objects.requireNonNull;

import app.packed.telemetry.span.Span;
import app.packed.util.Nullable;

/**
 * Uhh
 */
// https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk/trace/src/main/java/io/opentelemetry/sdk/trace/SdkSpan.java
public final class PackedSpan implements Span {
    volatile boolean isActive;

    final String name;

    final PackedTracer tracer;
    @Nullable
    final PackedSpan parent;

    PackedSpan(PackedTracer tracer, @Nullable PackedSpan parent, String name) {
        this.name = requireNonNull(name);
        this.tracer = requireNonNull(tracer);
        this.parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return isActive;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public Span newSpan(String name) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Builder newSpanBuilder(String name) {
        return null;
    }
}
