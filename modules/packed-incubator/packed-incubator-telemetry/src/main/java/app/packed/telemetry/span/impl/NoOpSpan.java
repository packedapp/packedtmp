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

import app.packed.telemetry.span.Span;

/**
 *
 */
public final class NoOpSpan extends AbstractSpan {

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return "noop";
    }

    /** {@inheritDoc} */
    @Override
    public Span newSpan(String name) {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Builder newSpanBuilder(String name) {
        return null;
    }
}
