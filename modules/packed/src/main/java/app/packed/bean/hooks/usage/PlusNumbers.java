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
package app.packed.bean.hooks.usage;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.operation.dependency.DependencyProvider;

/**
 *
 */
public class PlusNumbers extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(Usage.class);
    }

    public static void main(String[] args) {
        App.run(new PlusNumbers());
    }

    public static class MyExt extends Extension<MyExt> {

        @Override
        protected void hookOnBeanDependencyProvider(DependencyProvider injector) {
            System.out.println("DEV");
            // checkType
            Plus p = injector.variable().getAnnotation(Plus.class);
            injector.provideInstance(p.left() + p.right());
        }

    }

    @DependencyProvider.Hook(extension = MyExt.class)
    public @interface Plus {
        int left();

        int right();
    }

    public static class Usage {

        public Usage(@Plus(left = 123, right = 4545) int valc) {
            System.out.println(valc);
        }
    }
}
