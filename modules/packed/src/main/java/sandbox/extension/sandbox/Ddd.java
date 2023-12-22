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
package sandbox.extension.sandbox;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanKind;
import app.packed.container.BaseAssembly;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.ExtensionMetaHook.AnnotatedBeanMethodHook;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.lifetime.LifecycleOrder;
import app.packed.lifetime.OnInitialize;
import sandbox.extension.container.ContainerHandle;
import sandbox.extension.container.ContainerTemplate;

/**
 *
 */
public class Ddd extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Oi.class);
      //  use(MyEntityException.class).addEntityBean(String.class);
    }

    public static void main(String[] args) {
        App.run(new Ddd());
        ApplicationMirror m = App.mirrorOf(new Ddd());
        System.out.println(Ddd.class.getCanonicalName() + ".build(Ddd.java:44)");
        m.print();
    }

    public static class Foo {
        Foo() {
            System.out.println("New foo");
        }
    }

    public static class MyEntityException extends Extension<MyEntityException> {
        MyEntityException child;

        MyEntityException() {}

        public void addEntityBean(Class<?> entityBean) {
            child().base().newBeanForUser(BeanKind.MANANGED.template()).install(entityBean);
        }

        MyEntityException child() {
            MyEntityException c = child;
            if (c == null) {
                ContainerHandle h = base().newContainer(ContainerTemplate.DEFAULT).named("EntityBeans").buildAndUseThisExtension();
                c = child = fromHandle(h).get();
            }
            return c;
        }

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @Override
                public void hookOnAnnotatedMethod(Annotation hooks, BeanMethod on) {
              //      base().runOnBeanInject(on.newDelegatingOperation());

                   // base().runOnBeanInject(on.newOperation());
                }
            };
        }

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedBeanMethodHook(allowInvoke = true, extension = MyEntityException.class)
    public @interface MyOnInitialize {

        /**
         * @return whether or not the annotated method should be run before or after dependencies in the same lifetime are
         *         initialized.
         */
        LifecycleOrder ordering() default LifecycleOrder.BEFORE_DEPENDENCIES;
    }

    public static class Oi {

        @OnInitialize
        public void onInit() {
            System.out.println("ASDASD");
        }

        @MyOnInitialize
        public void onInixt() {
            System.out.println("xxASDASD");
        }
    }
}
