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
package app.packed.operation.bindings;

import java.util.List;

import app.packed.operation.BindingMirror;

/**
 * A composite binding mirror
 */
public class CompositeBindingMirror extends BindingMirror {

    public List<BindingMirror> bindings() {
        throw new UnsupportedOperationException();
    }

    // Tror ikke laengere vi bliver resolved som en compond.
    // get(Req, Res) -> Har bare 2 parametere. (Maaske idk)
    public boolean isFuncionalInterface() {
        throw new UnsupportedOperationException();
    }

}
