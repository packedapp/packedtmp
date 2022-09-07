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
package archive.bean.dependency;

import java.lang.reflect.Type;

/**
 *
 */
public interface ConvertedVariable {

    //// For Parameterized types
    // ConvertedVariable convertMore(int index);

    Type readType(); // raw class or array type

    Class<?> readClass(); // raw class or array type
}

// Type-Based injection... we kender ligesom "raw" typen, Det vi er interesseret i er parameterene...
// Map<K,V> -> Maa vaere en Converter<Variable, ConvertedVariable[]>

// 