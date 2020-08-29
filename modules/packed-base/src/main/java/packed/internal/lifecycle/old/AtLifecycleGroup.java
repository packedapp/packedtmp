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
package packed.internal.lifecycle.old;

import java.util.List;

import app.packed.lifecycleold.OnInitialize;
import app.packed.lifecycleold.OnStart;
import app.packed.lifecycleold.OnStop;

/**
 * Information about methods annotated with {@link OnInitialize}, {@link OnStart} and {@link OnStop}, typically on a
 * single class. Used mainly for components.
 */
public class AtLifecycleGroup {

    /** All non-static methods annotated with {@link OnInitialize}. */
    public final List<AtLifecycle> onInitialize;

    /** All non-static methods annotated with {@link OnInitialize}. */
    public final List<AtLifecycle> onStart;

    /** All non-static methods annotated with {@link OnInitialize}. */
    public final List<AtLifecycle> onStop;

    /**
     * Creates a new provides group
     * 
     * @param builder
     *            the builder to create the group for
     */
    private AtLifecycleGroup(Builder builder) {
        this.onInitialize = builder.onInitialize == null ? List.of() : List.copyOf(builder.onInitialize);
        this.onStart = builder.onStart == null ? List.of() : List.copyOf(builder.onStart);
        this.onStop = builder.onStop == null ? List.of() : List.copyOf(builder.onStop);
    }

    /** A builder for an {@link AtLifecycleGroup}. */
    public final static class Builder {

        /** All non-static methods annotated with {@link OnInitialize}. */
        List<AtLifecycle> onInitialize;

        /** All non-static methods annotated with {@link OnStart}. */
        List<AtLifecycle> onStart;

        /** All non-static methods annotated with {@link OnStop}. */
        List<AtLifecycle> onStop;
    }
}
