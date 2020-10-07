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
import packed.internal.sidecar.SidecarModel;
import packed.internal.sidecar.VariableSidecarModel.VariableSidecarConfiguration;

/**
 *
 */
public abstract class VariableSidecar {

    /** A sidecar configurations object. Updated by {@link SidecarModel.Builder}. */
    @Nullable
    private VariableSidecarConfiguration configuration;

    private VariableSidecarConfiguration configuration() {
        return configuration;
    }

    protected void configure() {}

    protected final void debug() {
        configuration().debug();
    }

    protected final void requireAssignableTo(Class<?> type) {

    }
}
