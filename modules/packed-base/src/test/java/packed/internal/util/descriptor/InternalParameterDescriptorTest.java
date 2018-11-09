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
package packed.internal.util.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static support.assertj.Assertions.npe;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.packed.util.Nullable;
import app.packed.util.ParameterDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;

/** Tests {@link InternalParameterDescriptor}. */
public class InternalParameterDescriptorTest {

    static Method M = Arrays.stream(InternalParameterDescriptorTest.class.getDeclaredMethods()).filter(m -> m.getName().equals("someMethod")).findFirst().get();
    static Parameter P1 = M.getParameters()[0];
    static Parameter P2 = M.getParameters()[1];

    ParameterDescriptor PD_OTHER_1 = (ParameterDescriptor) Proxy.newProxyInstance(InternalParameterDescriptorTest.class.getClassLoader(),
            new Class<?>[] { ParameterDescriptor.class }, (p, mm, a) -> {
                if (mm.getName().equals("newParameter")) {
                    return P1;
                }
                throw new AssertionError();
            });

    @Test
    public void ofParameterDescriptor() throws Exception {
        InternalParameterDescriptor ipd = InternalParameterDescriptor.of(P1);
        assertThat(InternalParameterDescriptor.of(ipd)).isSameAs(ipd);
        assertThat(InternalParameterDescriptor.of(PD_OTHER_1)).isEqualTo(ipd);
    }

    @Test
    public void ofParameter() throws Exception {
        npe(() -> InternalParameterDescriptor.of((Parameter) null), "parameter");

        InternalParameterDescriptor ipd = InternalParameterDescriptor.of(P1);

        assertThat(ipd.descriptorTypeName()).isEqualTo("parameter");

        assertThat(ipd).isEqualTo(ipd);
        assertThat(ipd).isEqualTo(InternalParameterDescriptor.of(P1));
        assertThat(ipd).isNotEqualTo(InternalParameterDescriptor.of(P2));

        assertThat(ipd).isEqualTo(PD_OTHER_1);
        assertThat(ipd).isNotEqualTo("");

        // ipd.annotations

        assertThat(ParameterDescriptor.of(P1).newParameter()).isEqualTo(P1);

    }

    public void someMethod(@Nullable Map<String, List<?>> s, int f) {}

}
