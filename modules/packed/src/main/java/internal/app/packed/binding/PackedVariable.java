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
package internal.app.packed.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.binding.Variable;
import internal.app.packed.util.PackedAnnotationList;

/** Implementation of {@link Variable}. */
public record PackedVariable(PackedAnnotationList annotations, Type type) implements Variable {

    public static PackedVariable of(Type type, Annotation[] annotations) {
        return new PackedVariable(new PackedAnnotationList(annotations), type);
    }

    public static PackedVariable ofType(Type type) {
        return new PackedVariable(PackedAnnotationList.EMPTY, type);
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
