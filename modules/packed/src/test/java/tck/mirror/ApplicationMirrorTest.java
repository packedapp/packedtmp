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
package tck.mirror;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import app.packed.application.ApplicationMirror;
import app.packed.application.BootstrapApp;
import app.packed.application.BuildGoal;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionMirror;
import tck.TckAssemblies;

/** Basic tests for {@link ApplicationMirror}. */
public class ApplicationMirrorTest extends AbstractMirrorTest {

    /** We cannot create a usable mirror ourselves. */
    @Test
    public void frameworkMustInitializeMirror() {
        frameworkMustInitialize(() -> new ApplicationMirror().assembly());
    }

    @Test
    public void customApplicationMirror() {
        class MyAppMirror extends ApplicationMirror {}
        /** An driver for creating App instances. */
        BootstrapApp<Void> ba = BootstrapApp.of(c -> {
            c.specializeMirror(MyAppMirror::new);
            c.managedLifetime();
        });

        assertThat(ba.mirrorOf(new TckAssemblies.HelloWorldAssembly())).isInstanceOf(MyAppMirror.class);
    }

    @Test
    public void helloWorldApp() {
        ApplicationMirror m = HW;

        // Default application mirror is ApplicationMirror
        assertSame(ApplicationMirror.class, m.getClass());

        assertIdenticalMirror(m, m.assembly().application());
        assertIdenticalMirror(m, m.container().application());

        /// BuildGoal
        assertEquals(BuildGoal.MIRROR, m.buildGoal());

        // Only BaseExtension is used
        assertThat(m.extensionTypes()).containsExactly(BaseExtension.class);
        assertThat(m.use(BaseExtensionMirror.class)).isNotNull();

        // Hashcode equals
        assertEquals(m, m.container().application());
        assertEquals(m.hashCode(), m.container().application().hashCode());

        assertEquals("HelloWorld", m.name());

        assertEquals(m.toString(), "Application:HelloWorld");
    }

}
