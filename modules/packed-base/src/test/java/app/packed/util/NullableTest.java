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
package app.packed.util;

import app.packed.inject.TypeLiteral;

/**
 * This test just tests that {@link Nullable} can be placed everywhere we need it.
 */
public class NullableTest {

    @Nullable
    String nullableField;

    @Nullable // no way to disable this, and then allow for type variable usage
    public NullableTest() {}

    public NullableTest(@Nullable String parame) {}

    @Nullable
    public void nullableReturnType(@Nullable String parame) {
        new TypeLiteral<@Nullable String>() {};
    }
}
