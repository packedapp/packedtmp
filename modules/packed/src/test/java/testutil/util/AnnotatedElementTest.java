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
package testutil.util;

import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 *
 */
abstract class AnnotatedElementTest {

    static void validateAnnotatedElement(AnnotatedElement expected, AnnotatedElement actual) {

        assertArrayEquals(expected.getAnnotations(), actual.getAnnotations());
        assertArrayEquals(expected.getDeclaredAnnotations(), actual.getDeclaredAnnotations());
        for (Annotation a : expected.getAnnotations()) {
            validateAnnotatedElement0(a.annotationType(), expected, actual);
        }
        for (Annotation a : expected.getDeclaredAnnotations()) {
            validateAnnotatedElement0(a.annotationType(), expected, actual);
        }
        validateAnnotatedElement0(Deprecated.class, expected, actual);
    }

    private static void validateAnnotatedElement0(Class<? extends Annotation> a, AnnotatedElement expected, AnnotatedElement actual) {
        assertThat(expected.getAnnotation(a)).isEqualTo(actual.getAnnotation(a));
        assertArrayEquals(expected.getAnnotationsByType(a), actual.getAnnotationsByType(a));
        assertThat(expected.getDeclaredAnnotation(a)).isEqualTo(actual.getDeclaredAnnotation(a));
        assertArrayEquals(expected.getDeclaredAnnotationsByType(a), actual.getDeclaredAnnotationsByType(a));
        assertThat(expected.isAnnotationPresent(a)).isEqualTo(actual.isAnnotationPresent(a));
    }

    static void validateMember(Member expected, Member actual) {
        assert expected.getDeclaringClass() == actual.getDeclaringClass();
        assert expected.getModifiers() == actual.getModifiers();
        assertThat(expected.getName()).isEqualTo(actual.getName());
        assertThat(expected.isSynthetic()).isEqualTo(actual.isSynthetic());

        assertThat(expected.getDeclaringClass()).isSameAs(actual.getDeclaringClass());
        assertThat(expected.getModifiers()).isEqualTo(actual.getModifiers());
        assertThat(expected.getName()).isEqualTo(actual.getName());
        assertThat(expected.isSynthetic()).isEqualTo(actual.isSynthetic());
    }
}
