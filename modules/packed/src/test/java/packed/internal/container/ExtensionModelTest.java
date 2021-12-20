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
package packed.internal.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;

/** Tests {@link ExtensionModel}. */
public class ExtensionModelTest {

    @Test
    public void normal() {
        Extension ne1 = ExtensionModel.of(NormalExtension.class).newInstance(null);
        Extension ne2 = ExtensionModel.of(NormalExtension.class).newInstance(null);
        assertThat(ne1).isNotNull();
        assertThat(ne1).isNotSameAs(ne2);

        Extension priv = ExtensionModel.of(PrivateExtension.class).newInstance(null);
        assertThat(priv).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Disabled
    // TODO fix exact exception types..
    public void fails() {
        // abstract class
        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> ExtensionModel.of(AbstractTestExtension.class).newInstance(null));
        a.isExactlyInstanceOf(InternalExtensionException.class);
        // TODO test messages

        @SuppressWarnings("rawtypes")
        Class c = ExtensionModelTest.class;
        // Does not extend Extension, this is only relevant if some user calls
        // ComponentConfiguration.use((Class) someNonExtensionClass.class);
        a = assertThatThrownBy(() -> ExtensionModel.of(c).newInstance(null));
        a.isExactlyInstanceOf(IllegalArgumentException.class);

        // inner class
        a = assertThatThrownBy(() -> ExtensionModel.of(InnerClassExtension.class).newInstance(null));
        a.isExactlyInstanceOf(InternalExtensionException.class);

        // Takes parameter
        a = assertThatThrownBy(() -> ExtensionModel.of(TakesParameterExtension.class).newInstance(null));
        a.isExactlyInstanceOf(InternalExtensionException.class);
    }

    @Test
    public void testGen() {
        // We should use codegen to test, for example, that stuff is open...
        // And unnamed, automatic modules...
    }

    public abstract class AbstractTestExtension extends Extension {}

    public class InnerClassExtension extends Extension {}

    public static final class TakesParameterExtension extends Extension {
        TakesParameterExtension(String s) {}
    }

    public static final class NormalExtension extends Extension {
        NormalExtension() {}
    }

    private static final class PrivateExtension extends Extension {
        private PrivateExtension() {}
    }
}
