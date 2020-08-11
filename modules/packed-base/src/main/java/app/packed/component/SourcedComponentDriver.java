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

import app.packed.inject.Factory;

/**
 *
 */
public interface SourcedComponentDriver<T, X extends ComponentConfiguration> {

    ComponentDriver<X> bindToInstance(T instance);

    ComponentDriver<X> bindToFactory(Factory<T> factory);

    static <T> SourcedComponentDriver<T, SingletonConfiguration<T>> singleton() {
        throw new UnsupportedOperationException();
    }
}

class StringBundle extends Bundle<SingletonConfiguration<String>> {

    protected StringBundle(String foo) {
        super(driver().bindToInstance(foo));
    }

    protected StringBundle(Factory<String> factory) {
        super(driver().bindToFactory(factory));
    }

    private static SourcedComponentDriver<String, SingletonConfiguration<String>> driver() {
        return SourcedComponentDriver.singleton();
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {}

    public static void main(String[] args) {
        SourcedComponentDriver<String, SingletonConfiguration<String>> singleton = SingletonConfiguration.singleton();
        ComponentDriver<SingletonConfiguration<Object>> bindToInstance = SingletonConfiguration.singleton().bindToInstance("fffo");
        ComponentDriver<SingletonConfiguration<String>> bindToInstance2 = singleton.bindToInstance("foo");
        System.out.println(bindToInstance);
        System.out.println(bindToInstance2);
    }
}