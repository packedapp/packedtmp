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
package app.packed.sidecar;

import app.packed.base.Nullable;
import packed.internal.sidecar.MethodSidecarModel.MethodSidecarConfiguration;
import packed.internal.sidecar.old.SidecarModel;

/**
 * Packed creates a single instance of a subclass and runs the {@link #configure()} method.
 */
public abstract class MethodSidecar {

    // Hver sidecar har sit eget context object...
    // Eneste maade at subclasses ikke kan faa fat it

    /** A sidecar configurations object. Updated by {@link SidecarModel}. */
    @Nullable
    private MethodSidecarConfiguration configuration;

    private MethodSidecarConfiguration configuration() {
        return configuration;
    }

    protected void configure() {}

    protected final void debug() {
        configuration().debug();
    }

}
