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
package pckd.internals.inject.factory;

import static support.assertj.Assertions.assertThatInternalFactory;

import org.junit.jupiter.api.Test;

import app.packed.inject.Key;
import pckd.internals.inject.factory.InternalFactory;
import pckd.internals.inject.factory.InternalFactoryInstance;

/** Tests {@link InternalFactoryInstance} */
public class InternalFactoryInstanceTest {

    @Test
    public void ofInstance() {
        InternalFactory<String> f = InternalFactoryInstance.of("foo");
        assertThatInternalFactory(f).is(new Key<String>() {});
        assertThatInternalFactory(f).hasBound(String.class, String.class);
        
    }
    
    @Test
    public void ofInstanceClass() {
        InternalFactory<String> f = InternalFactoryInstance.of("foo");
        assertThatInternalFactory(f).hasBound(String.class, String.class);
    }
}
