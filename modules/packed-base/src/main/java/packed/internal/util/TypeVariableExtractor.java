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
package packed.internal.util;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

/**
 *
 */

// Uhhh den bliver sjov at lave....
// Make public???
//
public class TypeVariableExtractor<T> {

    private final Class<?> baseClass;
    private final int index;

    TypeVariableExtractor(Class<?> baseClass, int index) {
        this.baseClass = requireNonNull(baseClass);
        this.index = index;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public T extract(Class<?> clazz) {
        if (Modifier.isInterface(baseClass.getModifiers())) {
            return (T) TypeVariableExtractorUtil.findTypeParameterFromInterface((Class) clazz, (Class) baseClass, index);
        } else {
            return (T) TypeVariableExtractorUtil.findTypeParameterFromSuperClass((Class) clazz, (Class) baseClass, index);
        }
    }
    //
    // public <F> F extractAsClass(Class<?> from, Class<F> assignableTo) {
    //
    // }

    public static TypeVariableExtractor<Type> rawClass(Class<?> from) {
        return rawClass(from, 0);
    }

    public static TypeVariableExtractor<Type> rawClass(Class<?> from, int parameterIndex) {
        return new TypeVariableExtractor<>(from, parameterIndex);
    }

    public static TypeVariableExtractor<List<Class<?>>> rawClass(Class<?> from, int... indexes) {
        throw new UnsupportedOperationException();
    }

    public static <T> TypeVariableExtractor<Type> rawClass(Class<?> from, int parameterIndex, Class<T> to) {
        // Giv mig en klasse der extender to
        throw new UnsupportedOperationException();
    }

    // Skal ikke have direkte support for f.eks. Key syntes jeg?
    // TypeVariable yes, men ikke key

    // OnKey

    //// PLAN
    // Convert all to use TVE, make sure tests run
    // Start moving some of checks into it

    // TypeVariableExtractException <----
}
