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
package app.packed.extension.bridge;

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.extension.Extension;
import app.packed.service.sandbox.ServiceExportsTransformer;

/**
 *
 */
public final class ELB2 {

    // Hmmmmmm, fraekt
    public ExtensionLifetimeBridge transformServices(@SuppressWarnings("exports") Consumer<ServiceExportsTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    public static <E extends Extension<E>> ELB2 of(MethodHandles.Lookup lookup, Class<E> extensionType, BiConsumer<? super E, ? super Context> consumer) {
        throw new UnsupportedOperationException();
    }

    public static <E extends Extension<E>> ELB2 of(MethodHandles.Lookup lookup, Class<E> extensionType, BiConsumer<? super E, ? super Context> consumer,
            Runnable onNotInstalled) {
        throw new UnsupportedOperationException();
    }

    public interface Context {
        void provide(Class<?> clazz);
    }
}
