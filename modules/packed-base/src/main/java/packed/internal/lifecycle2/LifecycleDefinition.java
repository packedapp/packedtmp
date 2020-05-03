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
package packed.internal.lifecycle2;

import java.util.List;

/**
 *
 */
public final class LifecycleDefinition {

    final List<String> listString;

    LifecycleDefinition(String... states) {
        listString = List.of(states);
    }

    public int indexOf(String state) {
        return listString.indexOf(state);
    }

    public String[] toArray() {
        return listString.toArray(i -> new String[i]);
    }

    public int numberOfStates() {
        return listString.size();
    }

    public String state(int index) {
        return listString.get(index);
    }

    public static LifecycleDefinition of(String... states) {
        return new LifecycleDefinition(states);
    }
}