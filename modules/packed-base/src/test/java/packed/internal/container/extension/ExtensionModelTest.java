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
package packed.internal.container.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import app.packed.container.extension.Extension;

/** Tests {@link ExtensionModel}. */
public class ExtensionModelTest {

    @Test
    public void normal() {
        NormalExtension ne1 = ExtensionModel.newInstance(NormalExtension.class, null);
        NormalExtension ne2 = ExtensionModel.newInstance(NormalExtension.class, null);
        assertThat(ne1).isNotNull();
        assertThat(ne1).isNotSameAs(ne2);

        PrivateExtension priv = ExtensionModel.newInstance(PrivateExtension.class, null);
        assertThat(priv).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void fails() {
        // abstract class
        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> ExtensionModel.newInstance(AbstractExtension.class, null));
        a.isExactlyInstanceOf(IllegalArgumentException.class);
        // TODO test messages

        @SuppressWarnings("rawtypes")
        Class c = ExtensionModelTest.class;
        // Does not extend Extension
        a = assertThatThrownBy(() -> ExtensionModel.newInstance(c, null));
        a.isExactlyInstanceOf(IllegalArgumentException.class);

        // inner class
        a = assertThatThrownBy(() -> ExtensionModel.newInstance(InnerClassExtension.class, null));
        a.isExactlyInstanceOf(IllegalArgumentException.class);

        // Takes parameter
        a = assertThatThrownBy(() -> ExtensionModel.newInstance(TakesParameterExtension.class, null));
        a.isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGen() {
        // We should use codegen to test, for example, that stuff is open...
        // And unnamed, automatic modules...
    }

    public abstract class AbstractExtension extends Extension {}

    public class InnerClassExtension extends Extension {}

    public static class TakesParameterExtension extends Extension {
        public TakesParameterExtension(String s) {}
    }

    public static class NormalExtension extends Extension {}

    private static class PrivateExtension extends Extension {
        private PrivateExtension() {}
    }
}
