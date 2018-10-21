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
package app.packed.inject;

/**
 * An abstract base class for both {@link TypeLiteral} and {@link Key}. This is typically used in places that can take
 * either a key or a type literal.
 */
public abstract class TypeLiteralOrKey<T> {

    /** Package private constructor */
    TypeLiteralOrKey() {}

    public abstract Class<? super T> getRawType();

    public abstract Key<T> toKey();
}
