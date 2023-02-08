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
import internal.app.packed.util.types.TypeUtil;

/**
 *
 */
public record PackedVariable(PackedAnnotationList annotations, Type getType) implements Variable {

    /** {@inheritDoc} */
    @Override
    public Class<?> getRawType() {
        return TypeUtil.rawTypeOf(getType);
    }

    public static PackedVariable of(AnnotatedType type) {
        return new PackedVariable(new PackedAnnotationList(type.getAnnotations()), type.getType());
    }

    public static PackedVariable ofRaw(Class<?> clazz) {
        return new PackedVariable(PackedAnnotationList.EMPTY, clazz);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Annotation[] annotations = annotations().toArray();
        sb.append(Stream.of(annotations).map(Annotation::toString).collect(Collectors.joining(" ")));
        if (annotations.length > 0) {
            sb.append(" ");
        }
        sb.append(getType().toString());
        return sb.toString();
    }
}
