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
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.lifecycle.LifecycleDependantOrder;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedMethod;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerTemplate;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
public class Ddd extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        new Exception().printStackTrace();
        install(Oi.class);
        // use(MyEntityException.class).addEntityBean(String.class);
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

    public static class MyEntityExtension extends Extension<MyEntityExtension> {
        /**
         * @param handle
         */
        protected MyEntityExtension(ExtensionHandle<MyEntityExtension> handle) {
            super(handle);
        }

        MyEntityExtension child;

        public void addEntityBean(Class<?> entityBean) {
            child().base().newBean(BeanKind.MANANGED.template()).install(entityBean, BeanHandle::new);
        }

        MyEntityExtension child() {
            MyEntityExtension c = child;
            if (c == null) {
                ContainerHandle<?> h = base().newContainer(ContainerTemplate.DEFAULT).named("EntityBeans").installAndUseThisExtension();
                c = child = fromContainerHandle(h).get();
            }
            return c;
        }

        @Override
        protected BeanIntrospector newBeanIntrospector() {
            return new BeanIntrospector() {

                @Override
                public void onAnnotatedMethod(Annotation hooks, BeanIntrospector.OnMethod on) {
                    // base().runOnBeanInject(on.newDelegatingOperation());

                    // base().runOnBeanInject(on.newOperation());
                }
            };
        }

    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @OnAnnotatedMethod(allowInvoke = true, extension = MyEntityExtension.class)
    public @interface MyOnInitialize {

        /**
         * @return whether or not the annotated method should be run before or after dependencies in the same lifetime are
         *         initialized.
         */
        LifecycleDependantOrder ordering() default LifecycleDependantOrder.BEFORE_DEPENDANTS;
    }

    public static class Oi {

        @Initialize
        public void onInit() {
            System.out.println("ASDASD");
        }

        @MyOnInitialize
        public void onInixt() {
            System.out.println("xxASDASD");
        }
    }
}
