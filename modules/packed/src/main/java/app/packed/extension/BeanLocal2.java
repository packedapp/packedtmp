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
package app.packed.extension;

import java.util.function.Supplier;

import internal.app.packed.bean.BeanSetup;

/**
 *
 */

//https://docs.oracle.com/en/java/javase/20/docs/api/jdk.incubator.concurrent/jdk/incubator/concurrent/ScopedValue.html#get()
interface BeanLocal2<T> {

    T get(BeanSetup b); // throws NoSuchElement

    boolean isSet();

    T orElse(BeanSetup b, T other);

    <X extends Throwable> T orElseThrow(BeanSetup b, Supplier<? extends X> exceptionSupplier);

    void set(BeanSetup b, T value);
}
