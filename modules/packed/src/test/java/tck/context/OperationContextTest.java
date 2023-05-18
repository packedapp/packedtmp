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
import app.packed.operation.OperationMirror;
import sandbox.extension.operation.OperationHandle;
import tck.AppAppTest;
import tck.HookExtension;
import tck.HookExtension.MethodHook;
import tck.context.ContextsHelpers.NoImplContext;

/**
 *
 */
public class OperationContextTest extends AppAppTest {

    @BeforeEach
    void setup() {
        hooks().onVariableType((cl, v) -> {
            assert (cl == NoImplContext.class);
            v.bindContextValue(NoImplContext.class);
        });

        hooks().onAnnotatedMethod((l, b) -> {
            OperationHandle h = b.newOperation(ContextsHelpers.NoImplContext.OTINT);

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
    public void mirrors() throws Throwable {
        assertThat(appMirror().container().contexts()).isEmpty();
        BeanMirror b = findSingleApplicationBean();
        assertThat(b.contexts()).isEmpty();
        OperationMirror om = findSingleOperation(b);
        assertThat(om.contexts()).hasSize(1);
        assertThat(om.contexts()).containsKey(NoImplContext.class);
        ContextMirror cm = om.contexts().get(NoImplContext.class);
        assertSame(NoImplContext.class, cm.contextClass());
        assertSame(HookExtension.class, cm.extensionClass());
//        cm.initiatingOperations();
        assertEquals(om, cm.scope());
    }

    @Test
    public void invokation() throws Throwable {

        assertEquals(123, (Integer) invoker().invoke(new NoImplContext(123)));
        assertEquals(3434, (Integer) invoker().invoke(new NoImplContext(3434)));
    }

}
