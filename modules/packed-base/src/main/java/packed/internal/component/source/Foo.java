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
package packed.internal.component.source;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicLong;

import app.packed.component.App;
import app.packed.container.BaseAssembly;
import app.packed.hooks.ClassHook;
import app.packed.hooks.FieldHook;
import app.packed.hooks.MethodHook;
import app.packed.hooks.RealMethodSidecarBootstrap;

/**
 *
 */
public class Foo extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("asdasdasd");
        provide(MyComp.class);
        System.out.println(extensions());
    }

    public static void main(String[] args) throws InterruptedException {
        App.of(new Foo());
    }

    @Target({ ElementType.METHOD, ElementType.FIELD })
    @Retention(RUNTIME)
    @FieldHook(bootstrap = FieldBootstrap.class)
    @MethodHook(bootstrap = MethodBootstrap.class)
    public @interface Hej {}

    public static class MyComp {

        @Hej
        private String foo = "123";

        public MyComp(String s) {
            System.out.println(s);
        }

        @Hej
        public void foo() {
            System.out.println("HEJ");
        }

        @Hej
        public void food() {
            System.out.println("HEJx");
        }
    }
}

class MethodBootstrap extends RealMethodSidecarBootstrap {

    static final AtomicLong al = new AtomicLong();

    @Override
    protected void bootstrap() {
        System.out.println(manageWithClassHook(ClassBootstrap.class));
        // bindParameterToArguement(0, 0);
    }
}

class FieldBootstrap extends FieldHook.Bootstrap {

    static final AtomicLong al = new AtomicLong();

    @Override
    protected void bootstrap() {
        System.out.println(manageBy(ClassBootstrap.class));
    }
}

class ClassBootstrap extends ClassHook.Bootstrap {

    final AtomicLong al = new AtomicLong();

    @Override
    protected void bootstrap() {
        System.out.println("CONF");
        System.out.println("----- Fields ------");
        for (FieldHook.Bootstrap b : fields()) {
            System.out.println(b.field());
        }
        System.out.println("----- Methods ------");
        for (MethodHook.Bootstrap b : methods()) {
            System.out.println(b.method());
        }
        System.out.println("-----");
//        for (var e : managedMethods(MethodBootstrap.class)) {
//            // Hmm, det er doeden til protected metoder jo...
//           // e.bindParameterToInvoker(0, 0);
//        }
    }
}
