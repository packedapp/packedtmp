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
package tck.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.container.Author;
import app.packed.operation.Op0;
import tck.AppAppTest;

/**
 *
 */
public class BeanInstallTest extends AppAppTest {

    @Test
    public void install() {
        record R() {}
        install(R.class);
        BeanMirror m = mirrors().bean();
        install0(R.class, m, BeanSourceKind.CLASS);
        assertThat(m.operations()).hasSize(1);

        install(new Op0<R>(() -> new R()) {});
        m = mirrors().bean();
        install0(R.class, m, BeanSourceKind.OP);
        assertThat(m.operations()).hasSize(1);

        installInstance(new R());
        m = mirrors().bean();
        install0(R.class, m, BeanSourceKind.INSTANCE);
        assertThat(m.operations()).isEmpty();
    }

    private static void install0(Class<?> bc, BeanMirror b, BeanSourceKind bsk) {
        assertEquals(bc, b.beanClass());
        assertEquals(bsk, b.beanSourceKind());
        assertThat(b.contexts()).isEmpty();
        assertThat(b.owner()).isSameAs(Author.application());
        assertEquals(b.container().lifetime(), b.lifetime());

        assertEquals("R", b.name());
        assertEquals("/R", b.path().toString());
    }

}
