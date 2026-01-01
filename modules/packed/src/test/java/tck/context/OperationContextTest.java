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
package tck.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import app.packed.bean.BeanMirror;
import app.packed.context.ContextMirror;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import tck.AppAppTest;
import tck.HookTestingExtension;
import tck.HookTestingExtension.MethodHook;
import tck.context.ContextsHelpers.NoImplContext;

/**
 *
 */
public class OperationContextTest extends AppAppTest {

    @BeforeEach
    void beforeEach() {
        hooks().onVariableType((cl, v) -> {
            assert (cl == NoImplContext.class);
            v.bindContext(NoImplContext.class);
        });

        hooks().onAnnotatedMethod((_, b) -> {
            OperationHandle<?> h = b.newOperation().template(ContextsHelpers.NoImplContext.OTINT).install(OperationHandle::new);

            add(h);
        });

        record BeanX() {

            @MethodHook
            public int foo(NoImplContext sc) {
                return sc.i();
            }
        }
        installInstance(new BeanX());
    }

    @Test
    public void mirrorsTest() throws Throwable {
        BeanMirror b = mirrors().bean();
        assertThat(b.contexts()).isEmpty();
        OperationMirror om = mirrors().findSingleOperation(b);
        assertThat(om.contexts()).hasSize(1);
        assertThat(om.contexts()).containsKey(NoImplContext.class);
        ContextMirror cm = om.contexts().get(NoImplContext.class);
        assertSame(NoImplContext.class, cm.contextClass());
        assertSame(HookTestingExtension.class, cm.extensionClass());
//        cm.initiatingOperations();
        assertEquals(om, cm.scope());
    }

    @Test
    public void invokation() throws Throwable {
        invoker().invokeEquals(123, new NoImplContext(123));
        invoker().invokeEquals(3434, new NoImplContext(3434));
    }
}
