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
package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.util.stream.Gatherer;

/**
 *
 */
public class StreamUtil {

    public static <T, R> Gatherer<T, ?, R> instanceOf(Class<R> type) {
        requireNonNull(type, "type");
        return Gatherer.of((_, element, downstream) -> {
            if (type.isInstance(element)) {
                return downstream.push(type.cast(element));
            }
            return true;
        });
    }

  //  // Usage:
    //Stream<Object> objectStream = ...;
    //Stream<String> stringStream = objectStream.gather(instanceOf(String.class));

}
