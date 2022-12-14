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
package app.packed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import app.packed.container.AssemblyMirror;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionMirror;

/**
 *
 */
public class ApplicationMirrorTest {

    @Test
    public void simple() {

        class MyAss extends BaseAssembly {

            /** {@inheritDoc} */
            @Override
            protected void build() {
                installInstance("YoDog");
            }
        }

        ApplicationMirror m = App.newMirror(new MyAss());

        /// AssemblyMirror
        AssemblyMirror asm = m.assembly();
        assertEquals(m, asm.application());
        assertSame(MyAss.class, asm.assemblyClass());
        assertThat(asm.children()).isEmpty();
        assertEquals(m.container(), asm.container());
        assertTrue(asm.isRoot());
        assertThat(asm.hooks()).isEmpty();
        assertThat(asm.parent()).isEmpty();

        /// BuildGoal
        assertEquals(BuildGoal.NEW_MIRROR, m.buildGoal());

        // ContainerMirror
        ContainerMirror cm = m.container();
        assertEquals(m, cm.application());
        assertEquals(asm, cm.assembly());
        // assertThat(cm.children()).isEmpty();
        assertEquals(m.extensionTypes(), cm.extensionTypes());

        assertThat(m.extensionTypes()).containsExactly(BaseExtension.class);

        // Equals HashCode
        assertEquals(m, m.container().application());
        assertEquals(m.hashCode(), m.container().application().hashCode());

        m.lifetime();
        assertNotNull(m.use(BaseExtensionMirror.class));

        // Name
        assertEquals(MyAss.class.getSimpleName(), "MyAss");

        // Use
        m.use(BaseExtensionMirror.class);
    }
}
