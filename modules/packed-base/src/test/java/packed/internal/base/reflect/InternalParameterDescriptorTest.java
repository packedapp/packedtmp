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
package packed.internal.base.reflect;

import static org.assertj.core.api.Assertions.assertThat;
import static testutil.assertj.Assertions.npe;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.packed.base.Nullable;
import app.packed.inject.sandbox.Injector;
import packed.internal.introspection.PackedParameterDescriptor;

/** Tests {@link PackedParameterDescriptor}. */
public class InternalParameterDescriptorTest {

    static Method M = Arrays.stream(InternalParameterDescriptorTest.class.getDeclaredMethods()).filter(m -> m.getName().equals("someMethod")).findFirst().get();
    static Parameter P1 = M.getParameters()[0];
    static Parameter P2 = M.getParameters()[1];

    @Test
    public void ofParameter() throws Exception {
        npe(() -> PackedParameterDescriptor.from((Parameter) null), "parameter");

        PackedParameterDescriptor ipd = PackedParameterDescriptor.from(P1);

        assertThat(ipd.descriptorTypeName()).isEqualTo("parameter");

        assertThat(ipd).isEqualTo(ipd);
        assertThat(ipd).isEqualTo(PackedParameterDescriptor.from(P1));
        assertThat(ipd).isNotEqualTo(PackedParameterDescriptor.from(P2));

        assertThat(ipd).isNotEqualTo("");

        // ipd.annotations

        // assertThat(ParameterDescriptor.of(P1).newParameter()).isEqualTo(P1);

    }

    public void someMethod(@Nullable Map<String, List<?>> s, int f) {}

//    /** {@link #test1()} and {@link #test2()} should both */
//    @Test
//    public void test1() throws Exception {
//        class Tmpx<T> {
//            @SuppressWarnings("unused")
//            Tmpx(List<?> l) {}
//        }
//        // Tmpx is a non-static class so first parameter is TypeLiteralTest
//        assertThat(LIST_WILDCARD).isEqualTo(TypeToken.fromParameter(Tmpx.class.getDeclaredConstructors()[0].getParameters()[1]).type());
//    }

    @Test
    public void test2() {
        class X {}

        Injector.configure(c -> {
            c.lookup(MethodHandles.lookup());
            c.provide(X.class);
            c.provideInstance(this);
        });
    }
}
