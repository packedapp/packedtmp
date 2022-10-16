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
package internal.app.packed.operation.newInject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.App;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.bean.BeanIntrospector;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.service.Provide;

/**
 *
 */
public class TestNew extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance("foo");
        provide(Fop.class);
    }

    public static void main(String[] args) {
        App.run(new TestNew());
        System.out.println("Bye");
    }

    public static class Fop {
        public Fop(String s) {

        }

        @Provide
        public int foo(@XX("Nice") String s) {
            return 34;
        }
    }

    public static class MyExt extends Extension<MyExt> {
        MyExt() {}

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @Override
                public void onBinding(OnBinding h) {
                    XX rr = h.annotations().readRequired(XX.class);
                    System.out.println(rr);
                    h.bind("foo");
                    System.out.println("Got h " + h.hookClass());
                }
            };
        }
    }

    @Target({ ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @BindingHook(extension = MyExt.class)
    @interface XX {
        String value();
    }
}
