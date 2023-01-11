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
package internal.app.packed.util.types;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 *
 */
// Field, Method, TypeVariable
// new Type
public abstract class TypeAnnotationConverter<T> extends OldTypeConverter<T> {

    /** {@inheritDoc} */
    @Override
    public T convert(Type t) {
        return convert(t, new NoAnnotations());
    }

    public abstract T convert(Type t, AnnotatedElement element);

    // Kig ogsaa paa noget spring...

    // Maaske er TypeVariableExtractor

    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/core/ResolvableType.html

    // Meta annotations

    // https://www.logicbig.com/tutorials/spring-framework/spring-web-mvc/meta-annotation.html
    // https://stackoverflow.com/questions/37120046/spring-framework-aliasfor-annotation-dilema
    // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/annotation/AnnotatedElementUtils.html

    static class NoAnnotations implements AnnotatedElement {

        static final Annotation[] EMPTY = new Annotation[0];

        /** {@inheritDoc} */
        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Annotation[] getAnnotations() {
            return EMPTY;
        }

        /** {@inheritDoc} */
        @Override
        public Annotation[] getDeclaredAnnotations() {
            return getAnnotations();
        }

    }
}
