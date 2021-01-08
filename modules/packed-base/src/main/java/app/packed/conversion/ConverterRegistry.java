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
package app.packed.conversion;

import app.packed.hooks.sandbox.VariableInjector;

/**
 * A collection of converters
 */
// Giver det mening ikke at kunne konvertere direkte???
// Altsaa fx ServiceRegistry er kun info
@VariableInjector
public interface ConverterRegistry {

    <T> T convert(Object o, Class<T> to);

    // Default installed via ServiceLoader
    // I don't know if I like it
    static ConverterRegistry defaults() {
        // does not support refreshing
        throw new UnsupportedOperationException();
    }

    static ConverterRegistry defaults(ClassLoader classLoader) {
        throw new UnsupportedOperationException();
    }
}
