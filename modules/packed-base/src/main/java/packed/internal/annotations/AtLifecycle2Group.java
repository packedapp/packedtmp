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
package packed.internal.annotations;

import java.util.List;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;

/**
 * Information about methods annotated with {@link OnHook}, typically on a single class. Used mainly for components.
 */
public class AtLifecycle2Group {

    // Allow List<AnnotatedFieldHook<Get>>

    /** All hook methods that takes a single {@link AnnotatedFieldHook} as a parameter. */
    public final List<AtLifecycle> annotatedFieldHooks;

    /** All hook methods that takes a single {@link AnnotatedMethodHook} as a parameter. */
    public final List<AtLifecycle> annotatedMethodHooks;

    /**
     * Creates a new provides group
     * 
     * @param builder
     *            the builder to create the group for
     */
    private AtLifecycle2Group(Builder builder) {
        this.annotatedFieldHooks = builder.annotatedFieldHooks == null ? List.of() : List.copyOf(builder.annotatedFieldHooks);
        this.annotatedMethodHooks = builder.annotatedMethodHooks == null ? List.of() : List.copyOf(builder.annotatedMethodHooks);
    }

    /** A builder for an {@link AtLifecycle2Group}. */
    public final static class Builder {

        /** All hook methods that takes a single {@link AnnotatedFieldHook} as a parameter. */
        List<AtLifecycle> annotatedFieldHooks;

        /** All hook methods that takes a single {@link AnnotatedMethodHook} as a parameter. */
        List<AtLifecycle> annotatedMethodHooks;
    }
}
