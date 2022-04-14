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
package app.packed.bean.hooks.usage.varinjector;

import app.packed.bean.hooks.BeanVarInjector;
import app.packed.extension.Extension;

/**
 *
 */
public class PlusNumbers {

    @BeanVarInjector.Hook(extension = MyExt.class)
    @interface Plus {
        int arg1();

        int arg2();
    }

    
    static class MyExt extends Extension<MyExt> {

        @Override
        protected void hookOnBeanVarInjector(BeanVarInjector injector) {
            // checkType
            Plus p = injector.variable().getAnnotation(Plus.class);
            injector.provideInstance(p.arg1() + p.arg2());
        }

    }

    static class Usage {
        void foo(@Plus(arg1 = 123, arg2 = 4545) int valc) {}
    }
}
