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
// X extends SourcedComponentConfiguration<T>
public interface SourcedComponentDriver<T, X extends ComponentConfiguration> {

    static <T> SourcedComponentDriver<T, SingletonConfiguration<T>> singleton() {
        throw new UnsupportedOperationException();
    }
}

class StringBundle extends Bundle<SingletonConfiguration<String>> {

    protected StringBundle(String foo) {
        super(SourcedComponentDriver.singleton(), foo);
    }

    protected StringBundle(Factory<String> factory) {
        super(SourcedComponentDriver.singleton(), factory);
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {}

}