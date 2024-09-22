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
package app.packed.container;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanTemplate.Installer;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;

/**
 *
 */
public class MyW extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        use(MyExt.class).install();
    }

    public static void main(String[] args) {
        App.run(new MyW());
    }

    public static class FFF {
        FFF(MyBeanMirror m) {}
    }

    static class MyBeanMirror extends BeanMirror {

        /**
         * @param handle
         */
        public MyBeanMirror(BeanHandle<?> handle) {
            super(handle);
        }

    }

    static class MyBeanHandle extends BeanHandle<BeanConfiguration> {

        /**
         * @param installer
         */
        public MyBeanHandle(Installer installer) {
            super(installer);
        }

        @Override
        protected MyBeanMirror newBeanMirror() {
            return new MyBeanMirror(this);
        }

    }

    static class MyExt extends Extension<MyExt> {

        /**
         * @param handle
         */
        protected MyExt(ExtensionHandle<MyExt> handle) {
            super(handle);
        }

        void install() {
            base().newBean(BeanKind.CONTAINER.template()).install(FFF.class, MyBeanHandle::new);
        }
    }
}
