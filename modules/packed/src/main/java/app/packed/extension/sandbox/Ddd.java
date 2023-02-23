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
package app.packed.extension.sandbox;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.bean.OnInitialize;
import app.packed.container.BaseAssembly;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanHook.AnnotatedMethodHook;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.bean.BeanTemplate;
import app.packed.extension.container.ContainerHandle;
import app.packed.extension.container.ContainerTemplate;
import app.packed.lifetime.LifetimeOrder;

/**
 *
 */
public class Ddd extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        install(Oi.class);
        use(MyEntityException.class).addEntityBean(String.class);
    }

    public static void main(String[] args) {
        App.run(new Ddd());
        ApplicationMirror m = App.mirrorOf(new Ddd());
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
            child().base().beanBuilder(BeanTemplate.EXTERNAL).install(entityBean);
        }

        MyEntityException child() {
            MyEntityException c = child;
            if (c == null) {
                ContainerHandle h = base().containerBuilder(ContainerTemplate.DEFAULT).named("EntityBeans").buildAndUseThisExtension();
                c = child = fromHandle(h).get();
            }
            return c;
        }

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @Override
                public void hookOnAnnotatedMethod(Annotation hooks, BeanMethod on) {
                    base().runOnBeanInject(on.newDelegatingOperation());
                }
            };
        }

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @AnnotatedMethodHook(allowInvoke = true, extension = MyEntityException.class)
    public @interface MyOnInitialize {

        /**
         * @return whether or not the annotated method should be run before or after dependencies in the same lifetime are
         *         initialized.
         */
        LifetimeOrder ordering() default LifetimeOrder.BEFORE_DEPENDENCIES;
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
