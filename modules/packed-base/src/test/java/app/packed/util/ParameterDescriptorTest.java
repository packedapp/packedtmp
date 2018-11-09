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
package app.packed.util;

import static org.assertj.core.api.Assertions.assertThat;
import static support.assertj.Assertions.npe;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import packed.internal.util.descriptor.InternalParameterDescriptorTest;

/** Tests {@link ParameterDescriptor}, most of the test are in {@link InternalParameterDescriptorTest}. */
public class ParameterDescriptorTest {

    public void someMethod(@Nullable Map<String, List<?>> s) {}

    @Test
    public void of() throws Exception {
        Parameter p = ParameterDescriptorTest.class.getMethod("someMethod", Map.class).getParameters()[0];
        npe(() -> ParameterDescriptor.of(null), "parameter");
        assertThat(ParameterDescriptor.of(p).newParameter()).isEqualTo(p);
    }
}
