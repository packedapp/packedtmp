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
package hooks.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle.AccessMode;

import org.junit.jupiter.api.Test;

import app.packed.extension.ContainerContext;
import app.packed.extension.operation.OperationHandle;
import app.packed.extension.operation.OperationTemplate;
import app.packed.operation.OperationTarget;
import tools.AnnoOnField.InstanceField;
import tools.AnnoOnField.StaticField;
import tools.H;
import tools.HExtension;

/**
 *
 */
public class OnAnnotatedFieldTest {

    @Test
    public void instanceFieldGet() throws Throwable {
        H t = H.of(c -> {
            c.onAnnotatedFieldHook((l, b) -> {
                InstanceField.validateFoo(l, b);

                OperationHandle h = b.newGetOperation(OperationTemplate.defaults());
                assertEquals(MethodType.methodType(String.class, ContainerContext.class), h.invocationType());
                assertSame(HExtension.class, h.operator());

                if (h.target() instanceof OperationTarget.OfField f) {
                    assertEquals(InstanceField.FOO, f.field());
                    assertSame(AccessMode.GET, f.accessMode());
                } else {
                    fail();
                }

                // TODO fix
               // assertEquals(h.type(), FunctionType.of(String.class));
                c.generate(h);
            });
            c.provide(InstanceField.class);
        });

        assertEquals("instance", t.invoke());
    }

    @Test
    public void instanceFieldGetSet() throws Throwable {
        H t = H.of(c -> {
            c.onAnnotatedFieldHook((l, b) -> {
                c.generate(b.newGetOperation(OperationTemplate.defaults()));
//                OperationHandle h = b.newSetOperation(OperationTemplate.defaults().withArg(String.class));
  //              assertEquals(MethodType.methodType(void.class, ExtensionContext.class, String.class), h.invocationType());

                // Hvordan fungere det med set, replace osv

                // Vi laver ikke forskellige template alt efter volatile, non-volatile.

                // Vil helst hellere ikke have 2 forskellige template for @Provide field og method

                // Men hvordan mapper vi saa fx CompareAndSet til de rigtige argumenter

                // c.generate(h);
            });
            c.provide(InstanceField.class);
        });

        assertEquals("instance", t.invoke());
    }

    @Test
    public void staticFieldGet() throws Throwable {
        H t = H.of(c -> {
            c.onAnnotatedFieldHook((l, b) -> {
                c.generate(b.newGetOperation(OperationTemplate.defaults()));
            });
            c.provide(StaticField.class);
        });

        assertEquals("static", t.invoke());
    }

}
