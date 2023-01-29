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
package app.packed.bindings;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
// Common on static interface methods on annotations
public final class QualifierUtil {

    /** You are not qualified to call this constructor. */
    private QualifierUtil() {}

    public static <T extends Annotation> T synthetic(MethodHandles.Lookup caller, Class<? extends Annotation> annotationType) {
        throw new UnsupportedOperationException();
    }

    public static <E, T extends Annotation> Function<E, T> syntheticFunction(MethodHandles.Lookup caller, Class<? extends Annotation> annotationType,
            Class<E> attributeType) {
        return syntheticFunction(caller, annotationType, attributeType, "value");
    }

    public static <T extends Annotation> Function<Map<String, ?>, T> syntheticMapper(MethodHandles.Lookup caller, Class<? extends Annotation> annotationType) {
        throw new UnsupportedOperationException();
    }

    public static <E, T extends Annotation> Function<E, T> syntheticFunction(MethodHandles.Lookup caller, Class<? extends Annotation> annotationType,
            Class<E> attributeType, String attributeName) {
        throw new UnsupportedOperationException();
    }

    public static <E, F, T extends Annotation> BiFunction<E, F, T> syntheticBiFunction(MethodHandles.Lookup caller, Class<? extends Annotation> annotationType,
            Class<E> attributeType1, String attributeName1, Class<E> attributeType2, String attributeName2) {
        throw new UnsupportedOperationException();
    }
}
