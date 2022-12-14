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
package app.packed.micro.application;

import app.packed.application.App;
import app.packed.bean.Inject;
import app.packed.container.BaseAssembly;

/**
 *
 */
public class Sss {
    static final int C = 1;

    public static class MyClass {
        public void foo1() {}

        @Inject
        public void boo2() {}

        public void Goo() {}
    }

    public static BaseAssembly of(int beanCount) {
        return new BaseAssembly() {

            @Override
            public void build() {
               // long start = System.nanoTime();
                for (int i = 0; i < beanCount; i++) {
                    base().multiInstallInstance(new MyClass());
                }
               // System.out.println((System.nanoTime() - start) / C);
            }
        };
    }

    public static void main(String[] args) {
        long start = System.nanoTime();
        App.run(of(C));
        System.out.println(System.nanoTime() - start);
    }
}
