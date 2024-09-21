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
package app.packed.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.extension.Extension.DependsOn;

/** Tests {@link ExtensionDescriptor}. */
public class ExtensionDescriptorTest {

    @Test
    public void empty() {
        ExtensionDescriptor ed = ExtensionDescriptor.of(EmptyExtension.class);
        assertThat(ed.dependencies()).containsExactly(BaseExtension.class);
        assertThat(ed.type()).isSameAs(EmptyExtension.class);
    }

    @Test
    public void various() {
        ExtensionDescriptor ed = ExtensionDescriptor.of(VariousExtension.class);
        assertThat(ed.dependencies()).containsExactlyInAnyOrder(BaseExtension.class, EmptyExtension.class);
        assertThat(ed.type()).isSameAs(VariousExtension.class);
    }

    @DependsOn(extensions = EmptyExtension.class)
    static class VariousExtension extends Extension<VariousExtension> {

        /**
         * @param handle
         */
        VariousExtension(ExtensionHandle handle) {
            super(handle);
        }

        // @ExposeAttribute(declaredBy = ExtensionDescriptorTest.class, name = "description")
        SomeContract expose() {
            return new SomeContract();
        }

        static class SomeContract {

            /** {@inheritDoc} */
            @Override
            public boolean equals(Object obj) {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public int hashCode() {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public String toString() {
                throw new UnsupportedOperationException();
            }
        }
    }

    static class EmptyExtension extends Extension<EmptyExtension> {

        /**
         * @param handle
         */
        protected EmptyExtension(ExtensionHandle handle) {
            super(handle);
        }
    }
}
