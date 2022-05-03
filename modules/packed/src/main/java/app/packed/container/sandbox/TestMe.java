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
package app.packed.container.sandbox;

import app.packed.application.App;
import app.packed.bean.BeanExtension;
import app.packed.container.Assembly;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;

/**
 *
 */
public class TestMe extends Assembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        container().use(MyExt.class);
        container().use(BeanExtension.class).installInstance("sdsd");
        container().link(new Child());
    }

    public static void main(String[] args) {
        App.mirrorOf(new TestMe());
    }

    public static class Child extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            use(MyExt.class).isLast = true;
            installInstance("sdsd");
        }

    }

    public static class MyExt extends Extension<MyExt> {

        public boolean isLast = false;

        @Override
        protected void onApplicationClose() {
            super.onApplicationClose();
        }

        @Override
        protected void onAssemblyClose() {
            System.out.println(isLast);
            new Exception().printStackTrace();
            super.onAssemblyClose();
        }

    }
}
