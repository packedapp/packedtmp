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
package packed.internal.invokers;

import app.packed.inject.TypeLiteral;

/**
 *
 */
public interface Invokable<T> {

    TypeLiteral<T> getType();

    @SuppressWarnings("unchecked")
    default Class<T> getRawType() {
        return (Class<T>) getType().getRawType();
    }

    boolean isNullable();

    boolean isFailable();

    T invoke(Object[] arguments);

    enum Type {
        UNSAFE_NULLABLE, /**/
        SUPER_SAFE_NULLABLE, /**/
        FIXED;
    }
}
// should invoke