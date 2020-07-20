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
package app.packed.base.reflect;

import static testutil.assertj.Assertions.npe;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.packed.base.Nullable;
import app.packed.introspection.ParameterDescriptor;
import packed.internal.base.reflect.InternalParameterDescriptorTest;

/** Tests {@link ParameterDescriptor}, most of the test are in {@link InternalParameterDescriptorTest}. */
public class ParameterDescriptorTest {

    public void someMethod(@Nullable Map<String, List<?>> s) {}

    @Test
    public void from() throws Exception {
        // Parameter p = ParameterDescriptorTest.class.getMethod("someMethod", Map.class).getParameters()[0];
        npe(() -> ParameterDescriptor.from(null), "parameter");
        // assertThat(ParameterDescriptor.of(p).newParameter()).isEqualTo(p);
    }
}
