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
import tck.TckContexts;
import tck.TckContexts.NoImplContext;
import testutil.tools.AnnoOnMethod;
import testutil.tools.TckApp;

/**
 *
 */
public class MinimalContextTest {

    @Test
    public void testIt() throws Throwable {
        record BeanX() {

            @AnnoOnMethod
            public void foo(NoImplContext sc, NoImplContext sc2) {
                assertEquals(123, sc.i());
            }
        }

        TckApp t = TckApp.of(c -> {
            c.hookOnVariableType((cl, v) -> {
                assert (cl == NoImplContext.class);
                v.bindContextValue(NoImplContext.class);
            });
            c.onAnnotatedMethodHook((l, b) -> {
                OperationHandle h = b.newOperation(TckContexts.NoImplContext.OT);
                c.generate(h);
            });
            c.provide(BeanX.class);
        });

        t.invoke(new NoImplContext(123));
    }

}
