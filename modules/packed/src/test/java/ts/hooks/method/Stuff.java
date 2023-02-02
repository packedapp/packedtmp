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
package ts.hooks.method;

import org.junit.jupiter.api.Test;

import app.packed.operation.OperationHandle;
import app.packed.operation.BeanOperationTemplate;
import ts.hooks.method.AnnotatedMethodHookTester.OnM;

/**
 *
 */
public class Stuff {

    @Test
    public void foo() {
        record R() {

            @OnM
            void foo() {}
        }
        AnnotatedMethodHookTester.process(c -> {
            System.out.println(c.operationType());
            OperationHandle oh = c.newOperation(BeanOperationTemplate.defaults());
            System.out.println(oh);
      //      oh.generateMethodHandle();
        }, R.class);
    }

}
