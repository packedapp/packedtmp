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
package app.packed.binding.sandbox;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import app.packed.bean.lifecycle.OnStart;

/**
 *
 */

// Maybe just LazyStarting
// LazyValue?
public interface Lazy<T> extends Supplier<T> {

    static <T> Lazy<T> of(Callable<T> callable) {
        throw new UnsupportedOperationException();
    }
}

class SomeBean {

    @OnStart(fork = true) // will it just get the field. and call get??? Catching Lazy Initialization exception
    final Lazy<String> l = Lazy.of(this::calc);

    private String calc() {
        return "afasdf";
    }

    @OnStart(fork = true)
    public void start() {
        l.get();
    }
}