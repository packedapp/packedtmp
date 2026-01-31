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
package app.packed.lifecycle.sandbox;

import java.util.Objects;

/** Options for stopping a lifetime. */
public sealed interface StopOptions {

    /** {@return new stop options with the specified failure cause} */
    StopOptions withFailure(Throwable cause);

    /** {@return the default stop options} */
    static StopOptions defaults() {
        return DefaultStopOptions.DEFAULT;
    }

    /** {@return stop options indicating a failure} */
    static StopOptions failure(Throwable cause) {
        Objects.requireNonNull(cause, "cause is null");
        return new DefaultStopOptions(cause);
    }
}

record DefaultStopOptions(Throwable failure) implements StopOptions {
    static final DefaultStopOptions DEFAULT = new DefaultStopOptions(null);

    @Override
    public StopOptions withFailure(Throwable cause) {
        Objects.requireNonNull(cause, "cause is null");
        return new DefaultStopOptions(cause);
    }
}
