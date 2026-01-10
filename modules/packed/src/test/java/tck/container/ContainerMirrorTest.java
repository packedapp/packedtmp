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
package tck.container;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import app.packed.application.ApplicationMirror;
import app.packed.container.ContainerMirror;
import tck.AppAppTest;
import tck.TckBeans.HelloMainBean;

/**
 *
 */
public class ContainerMirrorTest extends AppAppTest {

    /** We cannot create a usable ContainerMirror ourselves. */
    @Test
    public void frameworkMustInitializeMirror() {
//        assertFrameworkInitializes(() -> new ContainerMirror().application());
    }

    @Test
    public void helloWorldApp() {
        // Everything to do with extensions is tested in .extension
        installInstance(new HelloMainBean());

        ApplicationMirror m = mirrors().application();
        ContainerMirror c = m.container();
        // Default application mirror is ApplicationMirror
        mirrors().assertIdentical(m, c.application());
        mirrors().assertIdentical(m.assemblies().root(), c.assembly());
        mirrors().findSingleBean(c);


        assertEquals(m.name(), c.name());
    }
}
