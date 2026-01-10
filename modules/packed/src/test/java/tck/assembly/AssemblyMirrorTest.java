/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package tck.assembly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import app.packed.assembly.AssemblyMirror;
import tck.AppAppTest;
import tck.TckAssemblies;

/** Basic tests for {@link AssemblyMirror}. */
public class AssemblyMirrorTest extends AppAppTest {

    /** We cannot create a usable ApplicationMirror ourselves. */
    @Test
    public void frameworkMustInitializeMirror() {
     //   assertFrameworkInitializes(() -> new AssemblyMirror().application());
    }

    @Test
    public void helloWorldApp() {
        setup().assembleWith(new TckAssemblies.HelloWorldAssembly());
        AssemblyMirror m = mirrors().assembly();

        assertSame(TckAssemblies.HelloWorldAssembly.class, m.assemblyClass());
        assertThat(m.assemblyDuration()).isPositive();
        assertEquals(m.container(), m.application().container());
        assertThat(m.delegatedFrom()).isEmpty();
        assertThat(m.toString()).isEqualTo("Assembly:HelloWorld:/");
    }
}
