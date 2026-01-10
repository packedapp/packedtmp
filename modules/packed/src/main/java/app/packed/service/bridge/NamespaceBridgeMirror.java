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
package app.packed.service.bridge;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface NamespaceBridgeMirror<T> {
    T from();

    T to();

    // Key<String>, "was rekeyed from Key<String> to Key<@Name("String>> incoming"
    List<NamespaceBridgeOpMirror<T>> modifications();
    // is same

    // Would be nice to reuse it for Config (key=String)
    interface NamespaceBridgeOpMirror<T> {
        T from();

        T to();

        String action();

        Map<String, Object> properties();
    }
}
