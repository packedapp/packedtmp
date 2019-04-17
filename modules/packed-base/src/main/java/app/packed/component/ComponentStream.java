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
package app.packed.component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.container.Component;
import app.packed.util.TypeLiteral;

/**
 *
 */
public interface ComponentStream extends Stream<Component> {

    ComponentInstanceStream<?> instances();

    <T> ComponentInstanceStream<T> instances(Class<T> instanceType);

    <T> ComponentInstanceStream<T> instances(TypeLiteral<T> instanceType);

    /**
     * Returns a new list containing all of the components in this stream in the order they where encountered. Is identical
     * to invoking {@code stream.collect(Collectors.toList())}.
     * <p>
     * This is a <em>terminal operation</em>.
     *
     * @return a new list containing all of the components in this stream
     */
    default List<Component> toList() {
        return collect(Collectors.toList());
    }
}
