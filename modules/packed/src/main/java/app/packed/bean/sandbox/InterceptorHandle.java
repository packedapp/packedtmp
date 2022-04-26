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
package app.packed.bean.sandbox;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.extension.sandbox.ExtensionLookup;

/**
 *
 */

/// Fx en PrintNanoTime

// Det var super cool at vi kunne lave saadan nogle permanente nogen...
// Men vi har brug for info som regel....
// Fx lad os sige vi skal lave en metrics paa runtime...
// Saa er det jo en parameter der skal bindes paa build-time

public class InterceptorHandle {

// Create instans with variable.. Arg

// Uses target parameters?
// Uses services
// Uses around object
// Uses result
// Manipulates result

    /// Around on success
    /// Around on finally
    /// 2 Arounds, one on catch one on success (where catch takes a Class<Throwable> )
    // Finally
    // Use Exception

    public enum Type {
        BEFORE, AFTER, AROUND;
    }

    static Builder builder(ExtensionLookup lookup) {
        return new Builder(lookup);
    }

    public static class Builder {
        final ExtensionLookup lookup;

        Builder(ExtensionLookup lookup) {
            this.lookup = requireNonNull(lookup, "lookup is null");
        }

        Builder before(MethodHandle methodHandle) {
            return this;
        }

        // PerApplication-PerSite or PerBeanInstance-PerSite
        public Builder holder(Class<?> clazz) {
            /// Ideen er lidt at vi instantiere en af dem her

            throw new UnsupportedOperationException();
        }

        public InterceptorHandle build() {
            throw new UnsupportedOperationException();
        }
    }
}
