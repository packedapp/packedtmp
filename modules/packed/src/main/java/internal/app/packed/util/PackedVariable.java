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
package internal.app.packed.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.util.Variable;

/** Implementation of {@link Variable}. */
public record PackedVariable(PackedAnnotationList annotations, Type type) implements Variable {

    public static PackedVariable of(Annotation[] annotations, Type type) {
        return new PackedVariable(new PackedAnnotationList(annotations), type);
    }

    public static PackedVariable of(AnnotatedType type) {
        return new PackedVariable(new PackedAnnotationList(type.getAnnotations()), type.getType());
    }

    public static PackedVariable ofRaw(Class<?> clazz) {
        return new PackedVariable(PackedAnnotationList.EMPTY, clazz);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Stream.of(annotations.annotations()).map(Annotation::toString).collect(Collectors.joining(" ")));
        if (!annotations.isEmpty()) {
            sb.append(" ");
        }
        sb.append(type.toString());
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotated() {
        return !annotations.isEmpty();
    }
}
