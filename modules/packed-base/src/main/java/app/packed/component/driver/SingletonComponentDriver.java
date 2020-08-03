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
package app.packed.component.driver;

import app.packed.component.ComponentDriver;
import app.packed.component.SingletonConfiguration;
import app.packed.inject.Factory;

/**
 *
 */
public interface SingletonComponentDriver<C, X> {

    ComponentDriver<C> source(Class<X> factory);

    ComponentDriver<C> source(Factory<X> factory);

    ComponentDriver<C> sourceInstance(X source);

    static <C, X> SingletonComponentDriver<X, C> of() {
        throw new UnsupportedOperationException();
    }

    static <X> SingletonComponentDriver<SingletonConfiguration<X>, X> driver() {
        throw new UnsupportedOperationException();
    }
}

class DriverUser {

    public static void main(String[] args) {
        SingletonConfiguration<CharSequence> sc = wire(SingletonComponentDriver.driver(), "foo");
        sc.setName("GejHej");
    }

    static <C> C wire(ComponentDriver<C> driver) {
        return driver.newConfiguration();
    }

    static <C, X> C wire(SingletonComponentDriver<C, X> driver, X instance) {
        return wire(driver.sourceInstance(instance));
    }
}