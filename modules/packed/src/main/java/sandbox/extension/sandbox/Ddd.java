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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.Bean;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanLifetime;
import app.packed.bean.lifecycle.Initialize;
import app.packed.bean.scanning.BeanTrigger.OnAnnotatedMethod;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerTemplate;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import internal.app.packed.extension.BaseExtensionHostGuestBeanintrospector;
import internal.app.packed.lifecycle.DependantOrder;

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
        IO.println(Ddd.class.getCanonicalName() + ".build(Ddd.java:44)");
        m.print();
    }

    public static class Foo {
        Foo() {
            IO.println("New foo");
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
            child().base().newBean(BeanLifetime.MANANGED.template()).install(Bean.of(entityBean), BeanHandle::new);
        }

        MyEntityExtension child() {
            MyEntityExtension c = child;
            if (c == null) {
                ContainerHandle<?> h = base().newContainer(ContainerTemplate.DEFAULT).named("EntityBeans").installAndUseThisExtension();
                c = child = fromContainerHandle(h).get();
            }
            return c;
        }


    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @OnAnnotatedMethod(allowInvoke = true, introspector = BaseExtensionHostGuestBeanintrospector.class)
    public @interface MyOnInitialize {

        /**
         * @return whether or not the annotated method should be run before or after dependencies in the same lifetime are
         *         initialized.
         */
        DependantOrder ordering() default DependantOrder.RUN_BEFORE_DEPENDANTS;
    }

    public static class Oi {

        @Initialize
        public void onInit() {
            IO.println("ASDASD");
        }

        @MyOnInitialize
        public void onInixt() {
            IO.println("xxASDASD");
        }
    }
}
