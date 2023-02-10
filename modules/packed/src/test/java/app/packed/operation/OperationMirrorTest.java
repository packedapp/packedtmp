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
package app.packed.operation;

import static mirrors.MirrorHelpers.beanMirror;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.VarHandle.AccessMode;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanMirror;
import app.packed.util.FunctionType;
import tools.AnnoOnField.InstanceField;
import tools.H;
import tools.HExtension;

/**
 * Tests {@link OperationMirror}.
 * <p>
 * It is difficult to test OperationMirror directly. So instead we create an operation via standard APIs and test it.
 */
public class OperationMirrorTest {

    /**
     * Must call {@link OperationMirror#initialize(internal.app.packed.operation.OperationSetup)} before any other
     * operation.
     */
    @Test
    public void notInitialize() {
        assertThrows(IllegalStateException.class, () -> new OperationMirror().bean());
    }

    /** Tests a simple OperationMirror */
    @Test
    public void simple() {
        ApplicationMirror t = H.mirrorOf(c -> {
            c.onAnnotatedFieldHook((l, b) -> {
                OperationHandle h = b.newGetOperation(OperationTemplate.defaults());
                c.generate(h);
            });
            c.provideInstance(new InstanceField());
        });

        BeanMirror bm = beanMirror(t, InstanceField.class);
        List<OperationMirror> l = bm.operations().toList();
        assertEquals(1, l.size());

        OperationMirror m = l.get(0);
        assertEquals(bm, m.bean());
        assertEquals(HExtension.class, m.invokedBy());
        assertTrue(m.nestedIn().isEmpty());

        assertTrue(m.bindings().isEmpty());
        assertTrue(m.contexts().isEmpty());

        if (m.target() instanceof OperationTarget.OfField f) {
            assertEquals(InstanceField.FOO, f.field());
            assertSame(AccessMode.GET, f.accessMode());
        } else {
            fail();
        }

        assertEquals(m.type(), FunctionType.of(String.class));
    }

    @Test
    public void customOperationMirror() {
        class MyOpMirror extends OperationMirror {}
        ApplicationMirror t = H.mirrorOf(c -> {
            c.onAnnotatedFieldHook((l, b) -> {
                OperationHandle h = b.newGetOperation(OperationTemplate.defaults());
                h.specializeMirror(() -> new MyOpMirror());
                c.generate(h);
            });
            c.provideInstance(new InstanceField());
        });

        OperationMirror om = beanMirror(t, InstanceField.class).operations().findFirst().get();
        assertTrue(om instanceof MyOpMirror);
    }
}
