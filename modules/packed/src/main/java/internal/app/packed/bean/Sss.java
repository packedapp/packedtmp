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
package internal.app.packed.bean;

import app.packed.application.App;
import app.packed.bean.Inject;
import app.packed.container.BaseAssembly;

/**
 *
 */
@IntrospecHints(staticMethods = { @StaticMethod(target = Sss.class, name = "main", methodType = { void.class }),
        @StaticMethod(target = Sss.class, name = "main", methodType = { void.class }) })
public class Sss {
    static final int C = 100000;

    public static class MyClass {
        public void foo1() {}

        @Inject
        public void boo2() {}

        @Inject
        public void boo23() {}

        @Inject
        public void boo24() {}

        @Inject
        public void boo25() {}

        public void Goo() {}
    }

    public static BaseAssembly of(int beanCount) {
        return new BaseAssembly() {

            @Override
            public void build() {
                for (int i = 0; i < beanCount; i++) {
                    bean().multiInstallInstance(new MyClass());
                }
            }
        };
    }

    public static void main(String[] args) throws Exception {
        
        int i =34;
        i++;
        System.out.println(i);
        long start = System.nanoTime();
//        MethodHandle fs = MethodHandles.lookup().findStatic(Sss.class, "main", MethodType.methodType(void.class, String[].class));
//        Method m = MethodHandles.reflectAs(Method.class, fs);
//        System.out.println(m);
        App.run(of(C));
        long stop = System.nanoTime() - start;
        System.out.println(stop);
        System.out.println(stop / C);
    }
}

@interface IntrospecHints {
    StaticMethod[] staticMethods() default {};
}

@interface StaticMethod {
    Class<?> target();

    String name();

    Class<?>[] methodType();

    @interface Many {
        StaticMethod[] methods();
    }
}
