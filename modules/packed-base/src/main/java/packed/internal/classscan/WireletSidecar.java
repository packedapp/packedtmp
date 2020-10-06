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
package packed.internal.classscan;

import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.component.Wirelet;
import app.packed.inject.Provide2;
import app.packed.introspection.VariableDescriptor;
import app.packed.sidecar.VariableSidecar;

/**
 *
 */

// No-List -> Takes latest
// List -> Takes all assignable
public final class WireletSidecar extends VariableSidecar {

    @Override
    protected void configure() {
        // Wirelet or list
        requireAssignableTo(Wirelet.class);

        // Hvordan haandtere vi Optional????

        // Returnere vi noget i Optional?
        // Returnere vi null???
        // Og hvad med Valhalla
    }

    // Must specify a subclass of Wirelet...
    // Must

    // Assembly
    /// Vi extracter WireletTyper

    public Context doo(VariableDescriptor var) {
        // if wirelet annotated with extension member... VarDesc must have the same
        // ProvideContext
        throw new UnsupportedOperationException();
    }

    @Provide2
    public Object provide(ComponentConfiguration cc, Context context) {
        if (context.isList) {
            return List.of();
        } else {
            return null;
        }
    }

    static class Context {
        boolean isList;
        Class<? extends Wirelet> wireletType;
    }
}
