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
package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

/**
 *
 */
// ContainerMirror (Supplier)
// BeanMirror
// BindingMirror
// ApplicationMirror
// AssemblyMirror
// BeanLifetimeMirror
// ContainerLifetimeMirror
// OperationMirror

//// Speciel
// Extension
// ExtensionMirror
// BeanIntrospector

public final class MagicInitializer<T> {

    final ThreadLocal<Holder<T>> TL = new ThreadLocal<>();

    public T initialize() {
        Holder<T> holder = TL.get();
        T t = requireNonNull(holder.t);
        holder.t = null;
        return t;
    }

    public <S> S run(Supplier<S> supplier, T value) {
        Holder<T> h = new Holder<>();
        h.t = value;
        TL.set(h);
        try {
            return supplier.get();
        } finally {
            TL.remove();
        }
    }

    private static class Holder<T> {
        private T t;
    }

    // Take a get class for better error messages?
    // Or maybe even an error message
    public static <T> MagicInitializer<T> of() {
        return new MagicInitializer<>();
    }
}
