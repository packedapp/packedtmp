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
package app.packed.base;

import java.util.function.Supplier;

/**
 *
 */
/// Taget fra noget gammel kode
// 1. Look for class annotations, that changes the packlet system model

// 2. Look for packlet class annotations... Can change the "model"

// 3. Constructor needs to take 1+2 into consideration

// 3. Look for field and member annotations taking 1+2 into consideration

//
class KeyX implements Runnable {

    String sasdsad = "adasd";

    /** A cache of models. */
    Supplier<String> SUP = new Supplier<>() {

        @Override
        public String get() {
            return "ASDASD";
        }
    };

    KeyX(String gggg) {

    }

    void ofd() {}

    /** {@inheritDoc} */
    @Override
    public void run() {}
}
