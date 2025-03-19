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
package tck.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.VarHandle.AccessMode;
import java.util.List;

import org.junit.jupiter.api.Test;

import app.packed.bean.BeanMirror;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationTemplate;
import tck.AppAppTest;
import tck.HookTestingExtension;
import tck.HookTestingExtension.FieldHook.FieldPrivateInstanceString;

/**
 * Tests {@link OperationMirror}.
 * <p>
 * It is difficult to test OperationMirror directly. So instead we create an operation via standard APIs and test it.
 */
public class OperationMirrorTest extends AppAppTest {

    /**
     * Must call {@link OperationMirror#initialize(internal.app.packed.operation.OperationSetup)} before any other
     * operation.
     */
    @Test
    public void notInitialized() {
//        assertThrows(IllegalStateException.class, () -> new OperationMirror().bean());
    }

    /** Tests a simple OperationMirror */
    @Test
    public void simple() {
        hooks().onAnnotatedField((_, b) -> {
            OperationHandle<?> h = b.newGetOperation(OperationTemplate.defaults()).install(OperationHandle::new);
            add(h);
        });
        installInstance(new FieldPrivateInstanceString());

        BeanMirror bm = mirrors().bean();
        List<OperationMirror> l = bm.operations().toList();
        assertEquals(1, l.size());

        OperationMirror m = l.get(0);
        assertEquals(bm, m.bean());
        assertEquals(HookTestingExtension.class, m.installedByExtension());
        assertTrue(m.nestedIn().isEmpty());

        assertTrue(m.bindings().isEmpty());
        // assertTrue(m.contexts().isEmpty());

        if (m.target() instanceof OperationTarget.OfField f) {
            assertEquals(FieldPrivateInstanceString.FOO_FIELD, f.field());
            assertSame(AccessMode.GET, f.accessMode());
        } else {
            fail();
        }

        // TODO fix
        // assertEquals(FunctionType.of(String.class), m.type());
    }

    @Test
    public void customOperationMirror() {
        class MyOpMirror extends OperationMirror {

            /**
             * @param handle
             */
            public MyOpMirror(OperationHandle<?> handle) {
                super(handle);
            }
        }
        class MyHandle extends OperationHandle<OperationConfiguration>{

            /**
             * @param installer
             */
            public MyHandle(OperationInstaller installer) {
                super(installer);
            }

            @Override
            protected OperationMirror newOperationMirror() {
                return new MyOpMirror(this);
            }
        }

        hooks().onAnnotatedField((_, b) -> {
            MyHandle h = b.newGetOperation(OperationTemplate.defaults()).install(MyHandle::new);
            add(h);
        });
        installInstance(new FieldPrivateInstanceString());

        OperationMirror om = mirrors().bean().operations().findFirst().get();
        assertTrue(om instanceof MyOpMirror);
    }
}
