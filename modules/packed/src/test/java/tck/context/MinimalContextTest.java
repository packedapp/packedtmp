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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import sandbox.extension.operation.OperationHandle;
import tck.AppAppTest;
import tck.HookExtension.MethodHook;
import tck.TckContexts;
import tck.TckContexts.NoImplContext;

/**
 *
 */
public class MinimalContextTest extends AppAppTest {

    @Test
    public void testIt() throws Throwable {
        record BeanX() {

            @MethodHook
            public int foo(NoImplContext sc) {
                return sc.i();
            }
        }

        hooks().onVariableType((cl, v) -> {
            assert (cl == NoImplContext.class);
            v.bindContextValue(NoImplContext.class);
        });

        hooks().onAnnotatedMethod((l, b) -> {
            OperationHandle h = b.newOperation(TckContexts.NoImplContext.OTINT);
            add(h);
        });
        install(BeanX.class);

       assertEquals(123, (Integer) invoker().invoke(new NoImplContext(123)));
       assertEquals(3434, (Integer) invoker().invoke(new NoImplContext(3434)));

    }

}
