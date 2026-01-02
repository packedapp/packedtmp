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
package tck.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionMirror;
import tck.AppAppTest;
import tck.TckAssemblies.HelloWorldAssembly;
import tck.TckBeans.HelloMainBean;

/**
 *
 */
public class ApplicationMirrorTest extends AppAppTest {

    /** We cannot create a usable mirror ourselves. */
    @Test
    public void frameworkMustInitializeMirror() {
        // assertFrameworkInitializes(() -> new ApplicationMirror().assemblies().root());
    }

    @Test
    public void test() {
        installInstance(new HelloMainBean());

        ApplicationMirror m = mirrors().application();

        // Default application mirror is ApplicationMirror
        assertSame(ApplicationMirror.class, m.getClass());

        mirrors().assertIdentical(m, m.assemblies().root().application());
        mirrors().assertIdentical(m, m.containers().root().application());

        // Only BaseExtension is used
        assertThat(m.extensionTypes()).containsExactly(BaseExtension.class);
        assertThat(m.use(BaseExtensionMirror.class)).isNotNull();

        // Hashcode equals
        assertEquals(m, m.container().application());
        assertEquals(m.hashCode(), m.container().application().hashCode());

        assertEquals("Assembly", m.name());

        assertEquals(m.toString(), "Application:Assembly");
    }

    /** Test that we can specialize an application mirror */
    @Test
    @Disabled
    public void specializeApplicationMirror() {
        // Test default application mirror type
        BootstrapApp<Void> ba = BootstrapApp.of(ApplicationTemplate.builder(Void.class).build());
        assertThat(ba.mirrorOf(new HelloWorldAssembly())).isExactlyInstanceOf(ApplicationMirror.class);

        // Specialize application mirror type
        class MyAppMirror extends ApplicationMirror {
            public MyAppMirror(ApplicationHandle<?, ?> handle) {
                super(handle);
            }
        }
        // specializeMirror(MyAppMirror::new).
        ba = BootstrapApp.of(ApplicationTemplate.builder(Void.class).build());
        assertThat(ba.mirrorOf(new HelloWorldAssembly())).isExactlyInstanceOf(MyAppMirror.class);
    }
}
