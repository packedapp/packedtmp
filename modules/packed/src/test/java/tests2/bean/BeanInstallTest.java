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
package tests2.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import tools.H;
import tools.TestAppMirror;

/**
 *
 */
public class BeanInstallTest {

    @Test
    public void install() {
        TestAppMirror m = H.mirrorOf(c -> c.install(R.class));
        BeanMirror b = m.bean();
        assertEquals(R.class, b.beanClass());
        assertEquals(BeanSourceKind.CLASS, b.beanSourceKind());
        assertEquals("R", b.name());
        assertEquals("/R", b.path().toString());
    }

    record R() {}
}
