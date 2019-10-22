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
package packed.internal.service.factory;

import static testutil.assertj.Assertions.checkThat;

import org.junit.jupiter.api.Test;

import app.packed.lang.Key;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.factoryhandle.InstanceFactoryHandle;

/** Tests {@link InstanceFactoryHandle} */
public class InternalFunctionInstanceTest {

    @Test
    public void ofInstance() {
        FactoryHandle<String> f = InstanceFactoryHandle.of("foo");
        checkThat(f).is(new Key<String>() {});
        checkThat(f).hasBound(String.class, String.class);
        
    }
    
    @Test
    public void ofInstanceClass() {
        FactoryHandle<String> f = InstanceFactoryHandle.of("foo");
        checkThat(f).hasBound(String.class, String.class);
    }
}
