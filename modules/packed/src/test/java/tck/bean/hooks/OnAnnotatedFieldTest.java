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
package tck.bean.hooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle.AccessMode;

import org.junit.jupiter.api.Test;

import app.packed.extension.ExtensionContext;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationTemplate;
import tck.AppAppTest;
import tck.HookTestingExtension;
import tck.HookTestingExtension.FieldHook.FieldPrivateInstanceString;
import tck.HookTestingExtension.FieldHook.FieldPrivateStaticString;

/**
 *
 */
public class OnAnnotatedFieldTest extends AppAppTest {

    @Test
    public void instanceFieldGet() throws Throwable {
        hooks().onAnnotatedField((l, b) -> {

            OperationHandle<?> h = b.newGetOperation(OperationTemplate.defaults()).install();
            assertEquals(MethodType.methodType(String.class, ExtensionContext.class), h.invocationType());
            assertSame(HookTestingExtension.class, h.operator());

            if (h.target() instanceof OperationTarget.OfField f) {
                assertEquals(FieldPrivateInstanceString.FOO_FIELD, f.field());
                assertSame(AccessMode.GET, f.accessMode());
            } else {
                fail();
            }

            // TODO fix
            // assertEquals(h.type(), FunctionType.of(String.class));
            add(h);
        });
        install(FieldPrivateInstanceString.class);

        assertEquals("instance", invoker().invoke());
    }

    @Test
    public void instanceFieldGetSet() throws Throwable {
        hooks().onAnnotatedField((l, b) -> {
            add(b.newGetOperation(OperationTemplate.defaults()).install());
//                OperationHandle h = b.newSetOperation(OperationTemplate.defaults().withArg(String.class));
            // assertEquals(MethodType.methodType(void.class, ExtensionContext.class, String.class), h.invocationType());

            // Hvordan fungere det med set, replace osv

            // Vi laver ikke forskellige template alt efter volatile, non-volatile.

            // Vil helst hellere ikke have 2 forskellige template for @Provide field og method

            // Men hvordan mapper vi saa fx CompareAndSet til de rigtige argumenter

            // c.generate(h);
        });
        install(FieldPrivateInstanceString.class);

        assertEquals("instance", invoker().invoke());
    }

    @Test
    public void staticFieldGet() throws Throwable {
        hooks().onAnnotatedField((l, b) -> {
            add(b.newGetOperation(OperationTemplate.defaults()).install());
        });

        install(FieldPrivateStaticString.class);

        assertEquals("static", invoker().invoke());
    }

}
