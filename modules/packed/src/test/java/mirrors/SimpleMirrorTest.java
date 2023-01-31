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
package mirrors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.application.BuildGoal;
import app.packed.container.AssemblyMirror;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;

/**
 *
 */
public class SimpleMirrorTest {

    @Test
    public void buildGoalMirro() {
        class MyAss extends BaseAssembly {

            /** {@inheritDoc} */
            @Override
            protected void build() {
                installInstance("HejHej");
            }
        }

        ApplicationMirror am = App.mirrorOf(new MyAss());

        /// AssemblyMirror
        AssemblyMirror asm = am.assembly();
        assertEquals(am, asm.application());
        assertSame(MyAss.class, asm.assemblyClass());
        assertThat(asm.children()).isEmpty();
        assertEquals(am.container(), asm.container());
        assertTrue(asm.isRoot());
        assertThat(asm.hooks()).isEmpty();
        assertThat(asm.parent()).isEmpty();

        /// BuildGoal
        assertEquals(BuildGoal.MIRROR, am.buildGoal());

        // ContainerMirror
        ContainerMirror cm = am.container();
        assertEquals(am, cm.application());
        assertEquals(asm, cm.assembly());
        //assertThat(cm.children()).isEmpty();
        assertEquals(am.extensionTypes(), cm.extensionTypes());


        // BeanMirror
        assertThat(cm.beans()).hasSize(1);

//        BeanMirror bm = cm.beans().iterator().next();

        assertThat(am.extensionTypes()).containsExactly(BaseExtension.class);

        // Equals HashCode
        assertEquals(am, am);

        am.lifetime();

        // Name
        assertEquals(MyAss.class.getSimpleName(), "MyAss");
    }
}
