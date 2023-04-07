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

import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

import app.packed.extension.BaseExtension;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import tck.TckBeans.HelloMainBean;

/**
 *
 */
public class OperationMirrorTest extends AbstractMirrorTest {

    /** We cannot create a usable mirror ourselves. */
    @Test
    public void frameworkMustInitializeMirror() {
        frameworkMustInitialize(() -> new OperationMirror().bean());
    }

    @Test
    public void helloWorldApp() {
        OperationMirror m = singleApplicationBean(HW).operations().findAny().get();

        assertIdenticalMirror(singleApplicationBean(HW), m.bean());

        assertThat(m.bindings()).isEmpty();

        assertThat(m.contexts()).isEmpty();

        assertThat(m.nestedIn()).isEmpty();

        assertThat(m.target()).isInstanceOf(OperationTarget.OfMethod.class);
        assertEquals(HelloMainBean.METHOD, ((OperationTarget.OfMethod) m.target()).method());

        // TODO I still don't completely understand entrypoints
//        assertIdenticalMirror(HW.container().lifetime(), m.entryPointIn().get());

        assertSame(BaseExtension.class, m.invokedBy());

        assertEquals("hello", m.name());

        assertEquals(MethodType.methodType(void.class), m.type().toMethodType());
    }
}
