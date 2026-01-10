/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package sandbox.typeconverter;

import java.lang.reflect.Type;

/**
 *
 */
// Replaced by converter project...
public abstract class OldTypeConverter<T> {

    public static final OldTypeConverter<Type> IDENTITY = new OldTypeConverter<>() {

        @Override
        public Type convert(Type t) {
            return t;
        }
    };

    public static final OldTypeConverter<Class<?>> RAW = null;

    public abstract T convert(Type t);
}
