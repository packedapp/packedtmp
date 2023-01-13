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
package internal.app.packed.operation;

import java.util.Stack;

/**
 *
 */
public final class IntStack {

    private final Stack<Integer> s = new Stack<>();

    public IntStack push(int i) {
        s.add(i);
        return this;
    }

    int size() {
        return s.size();
    }

    IntStack push(int... is) {
        for (int i : is) {
            s.add(i);
        }
        return this;
    }

    public int[] toArrayAdd0() {
        int[] result = new int[s.size() + 1];
        result[0] = 0;
        for (int i = 0; i < s.size(); i++) {
            result[i + 1] = s.get(i);
        }
        return result;
    }

    public int[] toArray() {
        int[] result = new int[s.size()];
        for (int i = 0; i < s.size(); i++) {
            result[i] = s.get(i);
        }
        return result;
    }
}
