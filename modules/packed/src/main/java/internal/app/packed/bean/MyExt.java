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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import app.packed.application.App;
import app.packed.bean.BeanExtensionPoint.MethodHook;
import app.packed.bean.BeanProcessor$BeanMethod;
import app.packed.bean.BeanProcessor;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;

/**
 *
 */
public class MyExt extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(MyBean.class);
    }

    public static void main(String[] args) {
        App.run(new MyExt());
    }

    static class My extends Extension<My> {

        @Override
        protected BeanProcessor newBeanScanner() {
            return new BeanProcessor() {

                @Override
                public void onProcessingStart() {
                    if (!beanClass().isAnnotationPresent(Deprecated.class)) {
               //         failWith("Beans that use @Foo must be annotated with @Deprecated");
                    }
                }

                @Override
                public void onMethod(BeanProcessor$BeanMethod method) {
                    System.out.println("Nice method " + method.method());
                }
            };
        }
    }

    public static class MyBean {

        @Foo
        public void foo() {

        }
    }

    @MethodHook(extension = My.class)
    @Retention(RUNTIME)
    @Documented
    @interface Foo {

    }
}
