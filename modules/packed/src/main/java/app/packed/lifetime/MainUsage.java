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
package app.packed.lifetime;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.operation.OperationMirror;

/**
 *
 */
public class MainUsage extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provide(MyBean.class);
        provideInstance("asdsd");
        provideInstance(123);
    }

    public static void main(String[] args) {
        long l = System.nanoTime();
        App.run(new MainUsage());
        App.run(new MainUsage());
        App.run(new MainUsage());
        System.out.println(System.nanoTime() - l);
    }

    public static class MyBean {

        public MyBean(OperationMirror am) {
            System.out.println(am.type());
        }

        @Main
        public void hello() {
            System.out.println("Hello");
        }
    }
}
